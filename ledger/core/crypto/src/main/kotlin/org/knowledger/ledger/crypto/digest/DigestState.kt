package org.knowledger.ledger.crypto.digest

import org.knowledger.collections.forEach
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import org.tinylog.kotlin.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.isSubclassOf

internal class DigestState(
    private val hashers: Hashers,
    private val nodes: ArrayDeque<DigestNode> = ArrayDeque(10),
    private val interningMap: InterningMap = LinkedHashMap(10),
    private val builderMap: BuilderInterningMap = LinkedHashMap(10),
) {
    private val hashSize = hashers.hashSize

    fun <T : Any> calculateHash(clazz: KClass<in T>): Hash {
        val initialNode = DigestNode(byteArrayOf(), clazz)
        initialNode.parent = initialNode
        nodes.addLast(initialNode)
        //Fill up states by breadth first search until all leaves have been resolved.
        var firstNode = nodes[0].state
        while (firstNode == DigestNode.State.FirstPass) {
            levelPass(nodes.takeWhile { it.state == DigestNode.State.FirstPass })
            firstNode = nodes[0].state
        }
        while (nodes.size > 1) {
            val temporaryStates = nodes.takeWhile { it.state != DigestNode.State.Composite }
            repeat(temporaryStates.size) { nodes.removeFirst() }
            nodes.first().compactLevel(temporaryStates)
        }
        return hashers.applyHash(nodes.removeFirst().typeHash)
    }

    private fun DigestNode.compactLevel(filteredNodes: List<DigestNode>) {
        val builder: StringBuilder
        if (this in builderMap) {
            builder = builderMap[this]!!
            builderMap.remove(this)
            processFiltered(builder, filteredNodes)
            compact(hashers.applyHash(builder.toString()))
        } else {
            builder = StringBuilder()
            builder.appendClassifier(classifier)
            processFiltered(builder, filteredNodes)
            compact(hashers.applyHash(builder.toString()))
        }
    }

    private fun DigestNode.processFiltered(
        builder: StringBuilder, filteredNodes: List<DigestNode>,
    ) {
        filteredNodes.forEach { node ->
            val stateBuilder = if (node.parent != this) {
                builderMap.getOrPut(node.parent, ::StringBuilder)
            } else {
                builder
            }
            stateBuilder.appendBytes(node.field).appendHash(node.typeHash)
        }
    }

    /**
     * Level pass does one level of a breadth first search, resolving final types or
     * expanding into their fields for later compacting.
     */
    private fun levelPass(firstPasses: List<DigestNode>) {
        firstPasses.forEach { node ->
            when (val classifier = node.classifier) {
                is KTypeParameter -> {
                    Logger.debug { "Level Pass on TypeParameter :> Current: ${classifier.name}" }
                    expandBounds(node, classifier)
                }
                is KClass<*> -> {
                    Logger.debug { "Level Pass on Class :> Current: ${classifier.qualifiedName}" }
                    node.checkEnum(classifier) || node.checkPrimitive(classifier) ||
                    node.checkHardcoded(classifier) || node.checkArrays(classifier) ||
                    expandFields(node, classifier)
                }
            }
        }
    }


    private fun DigestNode.checkEnum(clazz: KClass<*>): Boolean {
        if (clazz.isSubclassOf(Enum::class)) {
            enumDigest()
            return true
        }
        return false
    }


    private fun DigestNode.checkPrimitive(clazz: KClass<*>): Boolean =
        checkClass(clazz, primitiveClazzes) { index -> primitiveDigest(index) }

    private fun DigestNode.checkHardcoded(clazz: KClass<*>): Boolean =
        checkClass(clazz, hardCodedClazzes) { index -> hardcodedDigest(index) }

    private fun DigestNode.checkArrays(clazz: KClass<*>): Boolean =
        checkClass(clazz, arrayClazzes) { index -> arrayPass(index) }

    private fun checkClass(
        clazz: KClass<*>, array: Array<KClass<*>>, run: (Int) -> Unit,
    ): Boolean {
        val index = array.binarySearch(clazz, nameComparator)
        //Supplied class may have the same simple name.
        //There's only unique named classes in the used [array].
        //We must then be sure the supplied class is directly equal.
        if (index >= 0 && clazz == array[index]) {
            run(index)
            return true
        }
        return false
    }

    private fun DigestNode.arrayPass(componentIndex: Int) {
        val component = primitiveClazzes[componentIndex]
        markExpand()
        val digest = DigestNode("[${component.qualifiedName}".toByteArray(), component)
        digest.parent = this
        nodes.addFirst(digest)
    }

    private fun expandBounds(node: DigestNode, parameter: KTypeParameter) {
        val bounds = parameter.upperBounds
        if (bounds.isNotEmpty()) {
            node.markExpand()
            bounds.mapNotNull { type ->
                type.classifier?.let { classifier ->
                    DigestNode(byteArrayOf(), classifier).also { boundNode ->
                        boundNode.parent = node
                    }
                }
            }.forEach(nodes::addFirst)
        } else {
            node.compact(hashers.applyHash(parameter.name))
        }
    }


    private fun expandFields(node: DigestNode, clazz: KClass<*>): Boolean {
        if (clazz.members.isEmpty()) {
            node.emptyDigest()
            return true
        }

        node.markExpand()
        clazz.members.asSequence()
            .filterIsInstance<KProperty<*>>()
            .map { Pair(it, it.returnType.classifier) }
            .partition { (it.second as? KClass<*>) != node.classifier }
            .forEach(
                { (field, returnType) ->
                    expandWithArguments(field, returnType, node)
                },
                { (cycle, returnType) ->
                    expandCycle(cycle, returnType as KClass<*>, node)
                })
        return true
    }

    private fun expandCycle(
        cycle: KProperty<*>, returnType: KClass<*>, digestNode: DigestNode,
    ) {
        val digest = DigestNode(
            cycle.name.toByteArray(), returnType,
            DigestNode.State.Cycle, hardCodedHashes.last()(hashSize)
        )
        digest.parent = digestNode
        nodes.addFirst(digest)
    }

    private fun expandWithArguments(
        field: KProperty<*>, returnType: KClassifier?, digestNode: DigestNode,
    ) {
        when (returnType) {
            is KProperty<*> -> toString()
            is KClass<*> -> {
                val types = field.returnType.arguments
                    .mapNotNull { (it.type?.classifier) }
                    .toTypedArray()
                val nodeForField: DigestNode
                val statesForArguments: List<DigestNode>
                if (types.isNotEmpty()) {
                    nodeForField = DigestNode(
                        field.name.toByteArray(), returnType, DigestNode.State.FirstPass,
                        Hash.emptyHash, types
                    )
                    nodeForField.parent = digestNode
                    statesForArguments = types.map {
                        val digest = DigestNode(ByteArray(0), it)
                        digest.parent = nodeForField
                        digest
                    }
                } else {
                    nodeForField = DigestNode(field.name.toByteArray(), returnType)
                    nodeForField.parent = digestNode
                    statesForArguments = emptyList()
                }
                nodes.addFirst(nodeForField)
                statesForArguments.forEach(nodes::addFirst)
            }
        }
    }

    private fun DigestNode.emptyDigest(): DigestNode = apply {
        val kClass = classifier as KClass<*>
        compact(hashers.applyHash(kClass.qualifiedName!!))
    }

    private fun DigestNode.calculateDigest(
        interning: InterningEnum, digest: () -> Hash,
    ): DigestNode = apply {
        val hash = interningMap.getOrPut(interning, digest)
        compact(hash)
    }

    private fun DigestNode.enumDigest(): DigestNode =
        calculateDigest(InterningEnum.ENUM) {
            val kClass = classifier as KClass<*>
            hashers.applyHash(
                "${kClass.qualifiedName ?: ""}${kClass.members.joinToString { it.name }}"
            )
        }

    private fun DigestNode.primitiveDigest(index: Int): DigestNode =
        calculateDigest(internFromPrimitive(index)) {
            val kClass = primitiveClazzes[index]
            hashers.applyHash(kClass.qualifiedName!!)
        }


    private fun DigestNode.hardcodedDigest(index: Int): DigestNode =
        calculateDigest(internFromHardcoded(index)) {
            hardCodedHashes[index](hashSize)
        }

}

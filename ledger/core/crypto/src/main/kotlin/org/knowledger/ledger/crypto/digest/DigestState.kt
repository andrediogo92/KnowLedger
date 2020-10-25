package org.knowledger.ledger.crypto.digest

import org.knowledger.collections.forEach
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import org.tinylog.kotlin.Logger
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal class DigestState(
    private val hashers: Hashers,
    private val nodes: ArrayDeque<DigestNode> = ArrayDeque(10),
    private val interningMap: InterningMap = LinkedHashMap(10),
    private val builderMap: BuilderInterningMap = LinkedHashMap(10),
) {
    private val hashSize = hashers.hashSize

    fun <T : Any> calculateHash(clazz: KClass<in T>): Hash {
        val initialState = DigestNode(byteArrayOf(), clazz)
        initialState.parent = initialState
        nodes.addLast(initialState)
        //Fill up states by breadth first search until all leaves have been resolved.
        var firstState = nodes[0].state
        while (firstState == DigestNode.State.FirstPass) {
            levelPass(nodes.takeWhile { it.state == DigestNode.State.FirstPass })
            firstState = nodes[0].state
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
            builder.append(clazz!!.qualifiedName)
            processFiltered(builder, filteredNodes)
            compact(hashers.applyHash(builder.toString()))
        }
    }

    private fun DigestNode.processFiltered(
        builder: StringBuilder, filteredNodes: List<DigestNode>,
    ) {
        filteredNodes.forEach { state ->
            val stateBuilder = if (state.parent != this) {
                builderMap.getOrPut(state.parent, ::StringBuilder)
            } else {
                builder
            }
            stateBuilder.append(state.field.base64Encoded()).append(state.typeHash.base64Encoded())
        }
    }

    /**
     * Level pass does one level of a breadth first search, resolving final types or
     * expanding into their fields for later compacting.
     */
    private fun levelPass(firstPasses: List<DigestNode>) {
        firstPasses.forEach { digestState ->
            val clazz = digestState.clazz!!
            Logger.debug { "Level Pass :> Current: ${clazz.qualifiedName}" }
            when (clazz) {
                //UByteArray::class, UShortArray::class, UIntArray::class, ULongArray::class,
                Enum::class -> digestState.enumDigest()
                else -> {
                    checkPrimitive(clazz, digestState) ||
                    checkHardcoded(clazz, digestState) ||
                    checkArrays(clazz, digestState) ||
                    expandFields(digestState)
                }
            }
        }
    }


    private inline fun checkClass(
        clazz: KClass<*>, array: Array<KClass<*>>, run: (Int) -> Boolean,
    ): Boolean {
        val index = array.binarySearch(clazz, nameComparator)
        //Supplied class may have the same simple name.
        //There's only unique named classes in the used [array].
        //We must then be sure the supplied class is directly equal.
        return if (index >= 0 && clazz == array[index]) {
            run(index)
            true
        } else {
            false
        }
    }

    private fun checkPrimitive(clazz: KClass<*>, digestNode: DigestNode): Boolean =
        checkClass(clazz, primitiveClazzes) { index -> digestNode.primitiveDigest(index); true }

    private fun checkHardcoded(clazz: KClass<*>, digestNode: DigestNode): Boolean =
        checkClass(clazz, hardCodedClazzes) { index -> digestNode.hardcodedDigest(index); true }

    private fun checkArrays(clazz: KClass<*>, digestNode: DigestNode): Boolean =
        checkClass(clazz, arrayClazzes) { index -> digestNode.arrayPass(index); true }


    private fun DigestNode.arrayPass(componentIndex: Int) {
        val component = primitiveClazzes[componentIndex]
        markExpand()
        val digest = DigestNode("[${component.qualifiedName}".toByteArray(), component)
        digest.parent = this
        nodes.addFirst(digest)
    }

    private fun expandFields(digestNode: DigestNode): Boolean {
        val clazz = digestNode.clazz!!
        if (clazz.members.isEmpty()) {
            digestNode.emptyDigest()
            return true
        }

        digestNode.markExpand()
        clazz.members
            .filterIsInstance<KProperty<*>>()
            .sortedBy(KCallable<*>::name)
            .partition { (it.returnType.classifier as KClass<*>) != digestNode.clazz }
            .forEach(
                { field -> expandWithArguments(field, nodes, digestNode) },
                { cycle ->
                    val digest = DigestNode(
                        cycle.name.toByteArray(), cycle.returnType.classifier as KClass<*>,
                        DigestNode.State.Cycle, hardCodedHashes.last()(hashSize)
                    )
                    nodes.addFirst(digest)
                })
        return true
    }

    private fun expandWithArguments(
        field: KProperty<*>, nodes: ArrayDeque<DigestNode>, digestNode: DigestNode,
    ) {
        val clazz = field.returnType.classifier as KClass<*>
        val types = field.returnType.arguments
            .mapNotNull { (it.type?.classifier) }
            .filterIsInstance(KClass::class.java)
            .sortedBy { it.simpleName ?: it.toString() }
            .toTypedArray()
        val nodeForField: DigestNode
        val statesForArguments: List<DigestNode>
        if (types.isNotEmpty()) {
            nodeForField = DigestNode(
                field.name.toByteArray(), clazz, DigestNode.State.FirstPass, Hash.emptyHash, types
            )
            nodeForField.parent = digestNode
            statesForArguments = types.map {
                val digest = DigestNode(ByteArray(0), it)
                digest.parent = nodeForField
                digest
            }
        } else {
            nodeForField = DigestNode(field.name.toByteArray(), clazz)
            nodeForField.parent = digestNode
            statesForArguments = emptyList()
        }
        nodes.addFirst(nodeForField)
        statesForArguments.forEach(nodes::addFirst)
    }

    private fun DigestNode.emptyDigest(): DigestNode = apply {
        val kClass = clazz!!
        clazz = null
        compact(hashers.applyHash(kClass.qualifiedName!!))
    }

    private fun DigestNode.calculateDigest(
        interning: InterningEnum, digest: () -> Hash,
    ): DigestNode = apply {
        val hash = interningMap.getOrPut(interning, digest)
        clazz = null
        compact(hash)
    }

    private fun DigestNode.enumDigest(): DigestNode =
        calculateDigest(InterningEnum.ENUM) {
            val kClass = clazz!!
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

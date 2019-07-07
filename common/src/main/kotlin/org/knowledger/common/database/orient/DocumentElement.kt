package org.knowledger.common.database.orient

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob
import org.knowledger.common.data.Difficulty
import org.knowledger.common.data.Payout
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageBytes
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageID
import org.knowledger.common.hash.Hash
import org.knowledger.common.storage.adapters.Storable
import java.math.BigInteger

data class DocumentElement internal constructor(
    val elem: OElement
) : StorageElement, OElement by elem {

    override val identity: StorageID
        get() = DocumentID(elem.identity)

    override val presentProperties: MutableSet<String>
        get() = elem.propertyNames

    override val schema: String?
        get() = elem.schemaType.map {
            it.name
        }.orElse(null)

    override fun discard(): StorageElement =
        apply {
            elem.unload<OElement>()
        }

    override fun getDifficultyProperty(
        name: String
    ): Difficulty =
        Difficulty(
            BigInteger(
                elem.getProperty<OBlob>(name).toStream()
            )
        )


    override fun getPayoutProperty(
        name: String
    ): Payout =
        Payout(elem.getProperty(name))

    override fun getStorageIDs(
        name: String
    ): List<StorageID> =
        elem.getProperty<List<ORID>>(name).map {
            DocumentID(it)
        }

    override fun getMutableStorageIDs(
        name: String
    ): MutableList<StorageID> =
        elem.getProperty<List<ORID>>(name)
            .asSequence()
            .map {
                DocumentID(it)
            }.toMutableList()

    override fun getHashProperty(
        name: String
    ): Hash =
        Hash(elem.getProperty(name))

    override fun getHashList(
        name: String
    ): List<Hash> =
        elem.getProperty<List<ByteArray>>(name)
            .map {
                Hash(it)
            }

    override fun getMutableHashList(name: String): MutableList<Hash> =
        elem.getProperty<List<ByteArray>>(name)
            .asSequence()
            .map {
                Hash(it)
            }.toMutableList()


    override fun getHashSet(
        name: String
    ): Set<Hash> =
        elem.getProperty<Set<ByteArray>>(name)
            .asSequence()
            .map {
                Hash(it)
            }.toSet()

    override fun getMutableHashSet(
        name: String
    ): MutableSet<Hash> =
        elem.getProperty<Set<ByteArray>>(name)
            .asSequence()
            .map {
                Hash(it)
            }.toMutableSet()


    override fun getStorageBytes(
        name: String
    ): StorageBytes =
        DocumentBytes(elem.getProperty(name))

    override fun getElementList(
        name: String
    ): List<StorageElement> =
        elem.getProperty<List<OElement>>(name)
            .map {
                DocumentElement(it)
            }

    override fun getMutableElementList(name: String): MutableList<StorageElement> =
        elem.getProperty<List<OElement>>(name)
            .asSequence()
            .map {
                DocumentElement(it)
            }.toMutableList()


    override fun getElementSet(
        name: String
    ): Set<StorageElement> =
        mutableSetOf<StorageElement>().let { set ->
            elem.getProperty<Set<OElement>>(name)
                .mapTo(set) {
                    DocumentElement(it)
                }
        }

    override fun getMutableElementSet(
        name: String
    ): MutableSet<StorageElement> =
        elem.getProperty<Set<OElement>>(name)
            .asSequence()
            .map {
                DocumentElement(it)
            }.toMutableSet()


    override fun getElementMap(
        name: String
    ): Map<String, StorageElement> =
        elem.getProperty<Map<String, OElement>>(name)
            .mapValues {
                DocumentElement(it.value)
            }

    override fun getMutableElementMap(
        name: String
    ): MutableMap<String, StorageElement> =
        mutableMapOf<String, StorageElement>().let { map ->
            elem.getProperty<Map<String, OElement>>(name)
                .mapValuesTo(map) {
                    DocumentElement(it.value)
                }
        }


    override fun getLinked(
        name: String
    ): StorageElement =
        DocumentElement(elem.getProperty(name))


    override fun <T> getStorageProperty(
        name: String
    ): T =
        elem.getProperty(name)


    override fun setDifficultyProperty(
        name: String,
        difficulty: Difficulty,
        session: NewInstanceSession
    ): StorageElement =
        apply {
            val bytes = session.newInstance(
                difficulty.difficulty.toByteArray()
            ) as DocumentBytes
            elem.setProperty(
                name,
                bytes.blob
            )
        }

    override fun setHashProperty(
        name: String, hash: Hash
    ): StorageElement =
        apply {
            elem.setProperty(name, hash.bytes)
        }

    override fun setPayoutProperty(
        name: String, payout: Payout
    ): StorageElement =
        apply {
            elem.setProperty(name, payout.payout)
        }


    override fun setElementList(
        name: String, property: List<StorageElement>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                property.map {
                    (it as DocumentElement).elem
                }
            )
        }

    override fun setElementSet(
        name: String, property: Set<StorageElement>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                property
                    .asSequence()
                    .map {
                        (it as DocumentElement).elem
                    }.toSet()
            )
        }

    override fun setElementMap(
        name: String, property: Map<String, StorageElement>
    ): StorageElement =
        apply {
            elem.setProperty(name, property.mapValues {
                (it.value as DocumentElement).elem
            })
        }

    override fun setHashList(
        name: String, hashes: List<Hash>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                hashes.map { it.bytes }
            )
        }

    override fun setHashSet(
        name: String, hashes: Set<Hash>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                hashes
                    .asSequence()
                    .map { it.bytes }
                    .toSet()
            )
        }

    override fun setLinked(
        name: String, linked: StorageElement
    ): StorageElement =
        apply {
            linked as DocumentElement
            elem.setProperty(name, linked.elem)
        }

    override fun <T : Any> setLinked(
        name: String,
        storable: Storable<T>,
        element: T,
        session: NewInstanceSession
    ): StorageElement =
        apply {
            val stored = storable.store(
                element, session
            ) as DocumentElement
            elem.setProperty(name, stored.elem)
        }

    override fun <T> setStorageProperty(
        name: String, property: T
    ): StorageElement =
        apply {
            elem.setProperty(name, property)
        }

    override fun setStorageBytes(name: String, property: StorageBytes): StorageElement =
        apply {
            val blob = property as DocumentBytes
            elem.setProperty(name, blob.blob)
        }

    override fun print(): String =
        elem.toJSON()
}
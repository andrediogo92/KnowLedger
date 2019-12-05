package org.knowledger.ledger.database.orient

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob
import org.knowledger.collections.mapMutable
import org.knowledger.collections.mapMutableList
import org.knowledger.collections.mapMutableSet
import org.knowledger.collections.mapToSet
import org.knowledger.ledger.core.base.data.Difficulty
import org.knowledger.ledger.core.base.data.Payout
import org.knowledger.ledger.core.base.hash.Hash
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageBytes
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.database.adapters.Storable
import java.math.BigInteger

data class DocumentElement internal constructor(
    val elem: OElement
) : StorageElement, OElement by elem {

    override val identity: StorageID
        get() = DocumentID(elem.identity)

    override val json: String
        get() = elem.toJSON()

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

    override fun getHashProperty(
        name: String
    ): Hash =
        Hash(elem.getProperty(name))

    override fun getHashList(
        name: String
    ): List<Hash> =
        elem.getProperty<List<ByteArray>>(name)
            .map(::Hash)

    override fun getMutableHashList(name: String): MutableList<Hash> =
        elem.getProperty<List<ByteArray>>(name)
            .mapMutableList(::Hash)

    override fun getHashSet(
        name: String
    ): Set<Hash> =
        elem.getProperty<Set<ByteArray>>(name)
            .mapToSet(::Hash).toSet()

    override fun getMutableHashSet(
        name: String
    ): MutableSet<Hash> =
        elem.getProperty<Set<ByteArray>>(name)
            .mapMutableSet(::Hash)


    override fun getStorageBytes(
        name: String
    ): StorageBytes =
        DocumentBytes(elem.getProperty(name))

    override fun getElementList(
        name: String
    ): List<StorageElement> =
        elem.getProperty<List<OElement>>(name)
            .map(::DocumentElement)

    override fun getMutableElementList(name: String): MutableList<StorageElement> =
        elem.getProperty<List<OElement>>(name)
            .mapMutableList(::DocumentElement)

    override fun getElementListById(
        name: String
    ): List<StorageID> =
        elem.getProperty<List<ORID>>(name)
            .map(::DocumentID)

    override fun getMutableElementListById(
        name: String
    ): MutableList<StorageID> =
        elem.getProperty<List<ORID>>(name)
            .mapMutableList(::DocumentID)


    override fun getElementSet(
        name: String
    ): Set<StorageElement> =
        elem.getProperty<Set<OElement>>(name)
            .mapToSet(::DocumentElement)

    override fun getMutableElementSet(
        name: String
    ): MutableSet<StorageElement> =
        elem.getProperty<Set<OElement>>(name)
            .mapMutableSet(::DocumentElement)


    override fun getElementSetById(
        name: String
    ): Set<StorageID> =
        elem.getProperty<Set<ORID>>(name)
            .mapToSet(::DocumentID)

    override fun getMutableElementSetById(
        name: String
    ): MutableSet<StorageID> =
        elem.getProperty<Set<ORID>>(name)
            .mapMutableSet(::DocumentID)


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
        elem.getProperty<MutableMap<String, OElement>>(name)
            .mapMutable(::DocumentElement)


    override fun getElementMapById(
        name: String
    ): Map<String, StorageID> =
        elem.getProperty<Map<String, ORID>>(name)
            .mapValues {
                DocumentID(it.value)
            }

    override fun getMutableElementMapById(
        name: String
    ): MutableMap<String, StorageID> =
        elem.getProperty<MutableMap<String, ORID>>(name)
            .mapMutable(::DocumentID)


    override fun getLinked(
        name: String
    ): StorageElement =
        DocumentElement(elem.getProperty(name))

    override fun getLinkedById(
        name: String
    ): StorageID =
        DocumentID(elem.getProperty(name))


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

    override fun setElementListById(
        name: String, property: List<StorageID>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                property.map {
                    (it as DocumentID).id
                }
            )
        }

    override fun setElementSet(
        name: String, property: Set<StorageElement>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                property.mapToSet {
                    (it as DocumentElement).elem
                }
            )
        }

    override fun setElementSetById(
        name: String, property: Set<StorageID>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                property.mapToSet {
                    (it as DocumentID).id
                }
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

    override fun setElementMapById(
        name: String, property: Map<String, StorageID>
    ): StorageElement =
        apply {
            elem.setProperty(name, property.mapValues {
                (it.value as DocumentID).id
            })
        }


    override fun setHashList(
        name: String, hashes: List<Hash>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                hashes.map(Hash::bytes)
            )
        }

    override fun setHashSet(
        name: String, hashes: Set<Hash>
    ): StorageElement =
        apply {
            elem.setProperty(
                name,
                hashes.mapToSet(Hash::bytes)
            )
        }

    override fun setLinkedID(
        name: String, linked: StorageID
    ): StorageElement =
        apply {
            linked as DocumentID
            elem.setProperty(name, linked.id)
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

    override fun setStorageBytes(
        name: String, property: StorageBytes
    ): StorageElement =
        apply {
            val blob = property as DocumentBytes
            elem.setProperty(name, blob.blob)
        }

}
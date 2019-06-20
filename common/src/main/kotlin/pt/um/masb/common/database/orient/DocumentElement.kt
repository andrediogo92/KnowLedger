package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob
import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.data.Payout
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageBytes
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.storage.adapters.Storable
import java.math.BigInteger

data class DocumentElement internal constructor(
    val elem: OElement
) : StorageElement, OElement by elem {
    override fun getStorageIDs(
        name: String
    ): List<StorageID> =
        elem.getProperty<List<ORID>>(name).map {
            DocumentID(it)
        }

    override fun discard(): StorageElement =
        apply {
            elem.unload<OElement>()
        }

    override val presentProperties: MutableSet<String>
        get() = elem.propertyNames

    override val schema: String?
        get() = elem.schemaType.map {
            it.name
        }.orElse(null)


    override fun getDifficultyProperty(
        name: String
    ): Difficulty =
        Difficulty(
            BigInteger(
                elem.getProperty<OBlob>(name).toStream()
            )
        )

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

    override fun getHashSet(
        name: String
    ): MutableSet<Hash> =
        elem.getProperty<Set<ByteArray>>(name)
            .asSequence()
            .map {
                Hash(it)
            }.toMutableSet()


    override fun getPayoutProperty(
        name: String
    ): Payout =
        Payout(elem.getProperty(name))

    override val identity: StorageID
        get() = DocumentID(elem.identity)

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

    override fun getElementSet(
        name: String
    ): Set<StorageElement> =
        mutableSetOf<StorageElement>().apply {
            elem.getProperty<Set<OElement>>(name)
                .forEach {
                    this.add(DocumentElement(it))
                }
        }

    override fun getElementMap(
        name: String
    ): Map<String, StorageElement> =
        elem
            .getProperty<Map<String, OElement>>(name)
            .mapValues {
                DocumentElement(it.value)
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
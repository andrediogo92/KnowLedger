package pt.um.masb.common.database

import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.data.Payout
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.storage.adapters.Storable

interface StorageElement {
    val presentProperties: Set<String>
    val schema: String?
    val identity: StorageID

    fun getDifficultyProperty(name: String): Difficulty
    fun getHashProperty(name: String): Hash
    fun getPayoutProperty(name: String): Payout
    fun <T> getStorageProperty(name: String): T

    fun <T> setStorageProperty(
        name: String, property: T
    ): StorageElement

    fun getStorageBytes(name: String): StorageBytes
    fun getElementList(
        name: String
    ): List<StorageElement>

    fun getElementSet(
        name: String
    ): Set<StorageElement>

    fun getElementMap(
        name: String
    ): Map<String, StorageElement>

    fun getHashSet(name: String): MutableSet<Hash>
    fun getHashList(name: String): List<Hash>
    fun getLinked(name: String): StorageElement


    fun setDifficultyProperty(
        name: String,
        difficulty: Difficulty,
        session: NewInstanceSession
    ): StorageElement

    fun setHashProperty(
        name: String, hash: Hash
    ): StorageElement

    fun setHashList(
        name: String, hashes: List<Hash>
    ): StorageElement

    fun setHashSet(
        name: String, hashes: Set<Hash>
    ): StorageElement

    fun setPayoutProperty(
        name: String, payout: Payout
    ): StorageElement

    fun setLinked(
        name: String, linked: StorageElement
    ): StorageElement

    fun <T : Any> setLinked(
        name: String,
        storable: Storable<T>,
        element: T,
        session: NewInstanceSession
    ): StorageElement

    fun setElementList(
        name: String, property: List<StorageElement>
    ): StorageElement

    fun setElementSet(
        name: String, property: Set<StorageElement>
    ): StorageElement

    fun setElementMap(
        name: String,
        property: Map<String, StorageElement>
    ): StorageElement

    fun print(): String
}
package org.knowledger.ledger.database

import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.database.adapters.Storable

interface StorageElement :
    Discardable<StorageElement> {
    val presentProperties: Set<String>
    val schema: String?
    val identity: StorageID
    val json: String


    fun getDifficultyProperty(name: String): Difficulty
    fun getHashProperty(name: String): Hash
    fun getPayoutProperty(name: String): Payout
    fun <T> getStorageProperty(name: String): T
    fun getStorageBytes(name: String): StorageBytes


    fun getElementList(name: String): List<StorageElement>
    fun getMutableElementList(name: String): MutableList<StorageElement>
    fun getElementListById(name: String): List<StorageID>
    fun getMutableElementListById(name: String): MutableList<StorageID>


    fun getElementSet(name: String): Set<StorageElement>
    fun getMutableElementSet(name: String): MutableSet<StorageElement>
    fun getElementSetById(name: String): Set<StorageID>
    fun getMutableElementSetById(name: String): MutableSet<StorageID>


    fun getElementMap(name: String): Map<String, StorageElement>
    fun getMutableElementMap(name: String): MutableMap<String, StorageElement>
    fun getElementMapById(name: String): Map<String, StorageID>
    fun getMutableElementMapById(name: String): MutableMap<String, StorageID>


    fun getHashSet(name: String): Set<Hash>
    fun getMutableHashSet(name: String): MutableSet<Hash>


    fun getHashList(name: String): List<Hash>
    fun getMutableHashList(name: String): MutableList<Hash>


    fun getLinked(name: String): StorageElement
    fun getLinkedById(name: String): StorageID


    fun setDifficultyProperty(
        name: String, difficulty: Difficulty,
        session: NewInstanceSession
    ): StorageElement

    fun setDifficultyPropertyFromBytes(
        name: String, difficulty: StorageBytes
    ): StorageElement

    fun setPayoutProperty(name: String, payout: Payout): StorageElement
    fun setHashProperty(name: String, hash: Hash): StorageElement
    fun <T> setStorageProperty(name: String, property: T): StorageElement
    fun setStorageBytes(name: String, property: StorageBytes): StorageElement


    fun setHashList(name: String, hashes: List<Hash>): StorageElement
    fun setHashSet(name: String, hashes: Set<Hash>): StorageElement


    fun setLinkedID(name: String, linked: StorageID): StorageElement
    fun setLinked(name: String, linked: StorageElement): StorageElement
    fun <T : Any> setLinked(
        name: String, storable: Storable<T>,
        element: T, session: NewInstanceSession
    ): StorageElement


    fun setElementList(
        name: String, property: List<StorageElement>
    ): StorageElement

    fun setElementListById(
        name: String, property: List<StorageID>
    ): StorageElement

    fun setElementSet(
        name: String, property: Set<StorageElement>
    ): StorageElement

    fun setElementSetById(
        name: String, property: Set<StorageID>
    ): StorageElement

    fun setElementMap(
        name: String, property: Map<String, StorageElement>
    ): StorageElement

    fun setElementMapById(
        name: String, property: Map<String, StorageID>
    ): StorageElement
}
package org.knowledger.ledger.core.database

import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.storage.adapters.Storable

interface StorageElement :
    Discardable<StorageElement> {
    val presentProperties: Set<String>
    val schema: String?
    val identity: StorageID
    val json: String

    fun getDifficultyProperty(
        name: String
    ): org.knowledger.ledger.core.data.Difficulty
    fun getHashProperty(name: String): Hash
    fun getPayoutProperty(name: String): org.knowledger.ledger.core.data.Payout
    fun getStorageIDs(name: String): List<StorageID>
    fun getMutableStorageIDs(
        name: String
    ): MutableList<StorageID>
    fun <T> getStorageProperty(name: String): T
    fun getStorageBytes(name: String): StorageBytes
    fun getElementList(
        name: String
    ): List<StorageElement>

    fun getMutableElementList(
        name: String
    ): MutableList<StorageElement>
    fun getElementSet(
        name: String
    ): Set<StorageElement>

    fun getMutableElementSet(
        name: String
    ): MutableSet<StorageElement>
    fun getElementMap(
        name: String
    ): Map<String, StorageElement>

    fun getMutableElementMap(
        name: String
    ): MutableMap<String, StorageElement>

    fun getHashSet(name: String): Set<Hash>
    fun getMutableHashSet(
        name: String
    ): MutableSet<Hash>
    fun getHashList(name: String): List<Hash>
    fun getMutableHashList(
        name: String
    ): MutableList<Hash>
    fun getLinked(name: String): StorageElement



    fun setDifficultyProperty(
        name: String,
        difficulty: org.knowledger.ledger.core.data.Difficulty,
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
        name: String, payout: org.knowledger.ledger.core.data.Payout
    ): StorageElement

    fun <T> setStorageProperty(
        name: String, property: T
    ): StorageElement

    fun setStorageBytes(
        name: String, property: StorageBytes
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

}
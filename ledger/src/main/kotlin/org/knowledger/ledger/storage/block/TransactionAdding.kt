package org.knowledger.ledger.storage.block

import org.knowledger.ledger.storage.Transaction

internal interface TransactionAdding {
    /**
     * Add a single new transaction.
     *
     * Checks if block is sized correctly.
     *
     * Does not checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and correctly inserted.
     */
    operator fun plus(transaction: Transaction): Boolean

    /**
     * Add a single new transaction.
     *
     * Checks if block is sized correctly.
     *
     * Checks if the transaction is valid when [checkTransaction] is set.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and correctly inserted.
     */
    fun addTransaction(transaction: Transaction, checkTransaction: Boolean = false): Boolean
}
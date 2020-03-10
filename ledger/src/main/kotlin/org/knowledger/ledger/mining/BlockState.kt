package org.knowledger.ledger.mining

import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.NonceRegen

sealed class BlockState {
    data class BlockReady(
        val full: Boolean,
        internal val header: BlockHeader
    ) : BlockState() {
        val hashId
            get() = header.hash
        val merkleRoot
            get() = header.merkleRoot

        fun attemptMine(): MiningState =
            if (header.nonce == Long.MAX_VALUE) {
                MiningState.Refresh
            } else {
                (header as NonceRegen).newNonce()
                MiningState.Attempted
            }
    }

    object BlockNotReady : BlockState()

    object BlockFailure : BlockState() {
        override fun toString(): String =
            "Transaction does not verify"
    }
}
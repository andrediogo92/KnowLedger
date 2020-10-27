package org.knowledger.ledger.mining

import org.knowledger.ledger.storage.MutableBlockHeader

sealed class BlockState {
    data class BlockReady(
        val full: Boolean, internal val header: MutableBlockHeader,
    ) : BlockState() {
        val hashId get() = header.hash
        val merkleRoot get() = header.merkleRoot

        fun attemptMine(): MiningState =
            if (header.nonce == Long.MAX_VALUE) {
                MiningState.Refresh
            } else {
                header.newNonce()
                MiningState.Attempted
            }
    }

    object BlockNotReady : BlockState()

    object BlockFailure : BlockState() {
        override fun toString(): String = "Transaction does not verify"
    }
}
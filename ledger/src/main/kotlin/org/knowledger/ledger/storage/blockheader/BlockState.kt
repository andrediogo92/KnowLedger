package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.storage.BlockHeader

sealed class BlockState {
    data class BlockReady(
        internal val header: BlockHeader
    ) {
        val hashId
            get() = header.hash
        val merkleRoot
            get() = header.merkleRoot

        fun attemptMine(): MiningState =
            if (header.nonce == Long.MAX_VALUE) {
                MiningState.Refresh
            } else {
                header.newHash()
                MiningState.Attempted
            }
    }

    object BlockNotReady
}
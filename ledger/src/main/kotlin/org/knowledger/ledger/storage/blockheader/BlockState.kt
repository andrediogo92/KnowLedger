package org.knowledger.ledger.storage.blockheader

sealed class BlockState {
    data class BlockReady(
        internal val header: HashedBlockHeader
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
package org.knowledger.ledger.storage.blockheader

internal interface HashRegen {
    /**
     * New hash rehashes [BlockHeader] after a [BlockHeader.nonce]
     * increment.
     */
    fun updateHash()
}
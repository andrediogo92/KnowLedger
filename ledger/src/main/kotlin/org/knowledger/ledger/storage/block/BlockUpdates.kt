package org.knowledger.ledger.storage.block

internal interface BlockUpdates {
    fun updateHashes()
    fun newExtraNonce()
}
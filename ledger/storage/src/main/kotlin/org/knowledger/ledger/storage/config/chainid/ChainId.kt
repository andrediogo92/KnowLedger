package org.knowledger.ledger.storage.config.chainid

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.Tag

interface ChainId : Hashing, LedgerContract {
    val ledgerHash: Hash
    val tag: Tag
    val blockParams: BlockParams
    val coinbaseParams: CoinbaseParams
}
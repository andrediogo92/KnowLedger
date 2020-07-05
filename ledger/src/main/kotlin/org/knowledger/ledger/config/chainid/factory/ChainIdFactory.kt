package org.knowledger.ledger.config.chainid.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.service.CloningFactory

interface ChainIdFactory : CloningFactory<ChainId> {
    fun create(
        tag: Tag, ledgerHash: Hash, hash: Hash
    ): ChainId

    fun create(
        hasher: Hashers, encoder: BinaryFormat,
        tag: Tag, ledgerHash: Hash
    ): ChainId
}
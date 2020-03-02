package org.knowledger.ledger.config

import kotlinx.serialization.Serializable
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.serial.binary.ChainIdByteSerializer
import org.knowledger.ledger.service.ServiceClass

@Serializable(with = ChainIdByteSerializer::class)
interface ChainId : Hashing, ServiceClass {
    val tag: Tag
    val ledgerHash: Hash
    override val hash: Hash
}
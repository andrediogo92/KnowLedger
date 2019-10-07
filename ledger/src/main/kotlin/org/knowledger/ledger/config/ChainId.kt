package org.knowledger.ledger.config

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.serial.internal.ChainIdByteSerializer
import org.knowledger.ledger.service.ServiceClass

@Serializable(with = ChainIdByteSerializer::class)
interface ChainId : Hashing, ServiceClass {
    val tag: Tag
    val ledgerHash: Hash
    override val hash: Hash
}
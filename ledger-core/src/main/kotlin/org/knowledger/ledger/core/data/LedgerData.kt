package org.knowledger.ledger.core.data

import org.knowledger.ledger.core.serial.HashSerializable

interface LedgerData : Cloneable,
                       SelfInterval,
                       DataCategory,
                       HashSerializable {
    public override fun clone(): LedgerData
}
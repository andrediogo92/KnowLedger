package org.knowledger.ledger.core.base.data

import org.knowledger.ledger.core.base.serial.HashSerializable

interface LedgerData : Cloneable,
                       SelfInterval,
                       DataCategory,
                       HashSerializable {
    public override fun clone(): LedgerData
}
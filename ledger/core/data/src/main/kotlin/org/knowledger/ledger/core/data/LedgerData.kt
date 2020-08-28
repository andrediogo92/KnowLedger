package org.knowledger.ledger.core.data

interface LedgerData : Cloneable, SelfInterval, DataCategory, HashSerializable {
    public override fun clone(): LedgerData
}
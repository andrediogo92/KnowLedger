package org.knowledger.ledger.core.base.data


interface ConvertableUnit<T, in U : PhysicalUnit> {
    fun convertTo(value: T, to: U): T
}
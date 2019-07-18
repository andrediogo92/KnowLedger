package org.knowledger.ledger.core.data


interface ConvertableUnit<T, in U : PhysicalUnit> {
    fun convertTo(value: T, to: U): T
}
package pt.um.masb.common.data


interface ConvertableUnit<T, in U : PhysicalUnit> {
    fun convertTo(value: T, to: U): T
}
package pt.um.lei.masb.blockchain.data


interface ConvertableUnit<T, in U : PhysicalUnit> {
    fun convertTo(value: T, to: U): T
}
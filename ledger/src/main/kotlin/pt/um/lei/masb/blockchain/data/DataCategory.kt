package pt.um.lei.masb.blockchain.data

interface DataCategory {
    val dataConstant: Int
        get() = DataDefaults.DEFAULT_VALUABLE
}
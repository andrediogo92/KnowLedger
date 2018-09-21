package pt.um.lei.masb.blockchain.data

interface DataCategory {
    val dataConstant: Int
        get() = DATA_DEFAULTS.DEFAULT_VALUABLE
}
package pt.um.masb.common.data

interface DataCategory {
    val dataConstant: Int
        get() = DataDefaults.DEFAULT_VALUABLE
}
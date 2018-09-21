package pt.um.lei.masb.blockchain.data

import java.io.Serializable
import java.math.BigDecimal


class OtherData<T : Serializable>(
        val data: Serializable
) : BlockChainData<OtherData<T>> {

    override fun calculateDiff(previous: OtherData<T>): BigDecimal = BigDecimal.ONE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OtherData<*>) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        return result
    }

    override fun toString(): String {
        return "OtherData(data=$data)"
    }

    override val dataConstant: Int
        get() = DATA_DEFAULTS.DEFAULT_UNKNOWN
}

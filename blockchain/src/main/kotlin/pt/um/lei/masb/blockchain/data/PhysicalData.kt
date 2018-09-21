package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.Sizeable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DigestAble
import pt.um.lei.masb.blockchain.utils.GeoCoords
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant


data class PhysicalData<R : BlockChainData<R>>(
        val instant: Instant,
        val geoCoords: GeoCoords,
        val data: R
) : Sizeable, DigestAble, DataCategory by data, SelfInterval<R> by data {

    companion object {
        val MATH_CONTEXT = MathContext(8, RoundingMode.HALF_EVEN)
    }

    constructor(instant: Instant,
                lat: BigDecimal,
                lng: BigDecimal,
                data: R) : this(instant, GeoCoords(lat, lng), data)

    override fun digest(c: Crypter): String =
            c.applyHash("$instant${geoCoords.latitude}${geoCoords.longitude}$data")
}
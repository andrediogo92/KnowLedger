package pt.um.lei.masb.test

import assertk.assert
import assertk.assertions.isEqualTo
import kotlinx.serialization.cbor.CBOR
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.data.Loadable
import pt.um.lei.masb.blockchain.data.TUnit
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.persistance.loaders.LoaderManager
import pt.um.lei.masb.blockchain.persistance.loaders.PreConfiguredLoaders
import java.math.BigDecimal

/**
 * Serialization tests are not supported in IDE.
 * Will fail by default. Must be delegated to gradle.
 */
class TestSerialization {
    val temp = TemperatureData(
        BigDecimal.ONE,
        TUnit.CELSIUS
    )

    val tempSerial = TemperatureData.serializer()

    @Suppress("UNCHECKED_CAST")
    val loaderT: Loadable<TemperatureData> =
        PreConfiguredLoaders.loaders["Temperature"]
                as Loadable<TemperatureData>

    val loadSerial = Loadable.serializer(
        tempSerial
    )

    val loaders = LoaderManager

    @Test
    fun `Try Serialize loader`() {
        assert(
            loaderT
        ).isEqualTo(
            CBOR.load(
                loadSerial,
                CBOR.dump(loadSerial, loaderT)
            )
        )
    }

    @Test
    fun `Try Serialize data`() {
        assert(
            temp
        ).isEqualTo(
            CBOR.load(
                tempSerial,
                CBOR.dump(tempSerial, temp)
            )
        )
    }
}
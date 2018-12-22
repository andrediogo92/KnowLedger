package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import pt.um.lei.masb.blockchain.DataLoader
import pt.um.lei.masb.blockchain.data.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.Serializable

object PreConfiguredLoaders : Loaders {
    override val loaders: DataLoader by lazy {
        mutableMapOf(
            "Dummy" to Loadable {
                commonLoad(
                    it,
                    "Dummy"
                ) { DummyData() }
            },
            "Humidity" to Loadable {
                commonLoad(
                    it,
                    "Humidity"
                )
                {
                    val prop = it.getProperty<Byte>("unit")
                    val unit = when (prop) {
                        0x00.toByte() -> HUnit.G_BY_KG
                        0x01.toByte() -> HUnit.KG_BY_KG
                        0x02.toByte() -> HUnit.RELATIVE
                        else -> null
                    }
                    if (unit == null) {
                        throw LoadFailedException(
                            "HUnit is not one of the expected: $prop"
                        )
                    } else {
                        HumidityData(
                            it.getProperty("hum"),
                            unit
                        )
                    }
                }
            },
            "Luminosity" to Loadable {
                commonLoad(
                    it,
                    "Luminosity"
                ) {
                    val prop = it.getProperty<Byte>("unit")
                    val unit = when (prop) {
                        0x00.toByte() -> LUnit.LUMENS
                        0x01.toByte() -> LUnit.LUX
                        else -> null
                    }
                    if (unit == null) {
                        throw LoadFailedException(
                            "LUnit is not one of the expected: $prop"
                        )
                    } else {
                        LuminosityData(
                            it.getProperty("lum"),
                            unit
                        )
                    }
                }
            },
            "Noise" to Loadable {
                commonLoad(
                    it,
                    "Noise"
                ) {
                    val prop = it.getProperty<Byte>("unit")
                    val unit = when (prop) {
                        0x00.toByte() -> NUnit.DBSPL
                        0x01.toByte() -> NUnit.RMS
                        else -> null
                    }
                    if (unit == null) {
                        throw LoadFailedException(
                            "Unit is not one of the expected: $prop"
                        )
                    } else {
                        NoiseData(
                            it.getProperty("noiseLevel"),
                            it.getProperty("peakOrBase"),
                            unit
                        )
                    }
                }
            },
            "Other" to Loadable { elem ->
                commonLoad(
                    elem,
                    "Other"
                ) {
                    val bos = ByteArrayOutputStream()
                    val chunkIds: List<OIdentifiable> =
                        elem.getProperty("data")
                    for (id in chunkIds) {
                        val chunk = id.getRecord<ORecordBytes>()
                        chunk.toOutputStream(bos)
                        chunk.unload()
                    }

                    ObjectInputStream(
                        ByteArrayInputStream(
                            bos.toByteArray()
                        )
                    ).use {
                        OtherData(
                            it.readObject() as Serializable
                        )
                    }
                }
            },
            "Temperature" to Loadable {
                commonLoad(
                    it,
                    "Temperature"
                ) {
                    val prop = it.getProperty<Byte>("unit")
                    val unit = when (prop) {
                        0x00.toByte() -> TUnit.CELSIUS
                        0x01.toByte() -> TUnit.FAHRENHEIT
                        0x02.toByte() -> TUnit.KELVIN
                        0x03.toByte() -> TUnit.RANKINE
                        else -> null
                    }
                    if (unit == null) {
                        throw LoadFailedException(
                            "TUnit is not one of the expected: $prop"
                        )
                    } else {
                        TemperatureData(
                            it.getProperty("temperature"),
                            unit
                        )
                    }
                }
            }
        )
    }

    private fun <T> commonLoad(
        document: OElement,
        tName: String,
        loader: () -> T
    ): T {
        val opt = document.schemaType.map { it.name }
        val name = if (opt.isPresent) {
            opt.get()
        } else {
            throw LoadFailedException(
                "Schema not existent for $document"
            )
        }
        return if (tName == name) {
            loader()
        } else {
            throw LoadFailedException(
                "Got document with unexpected class: $name"
            )
        }
    }

}
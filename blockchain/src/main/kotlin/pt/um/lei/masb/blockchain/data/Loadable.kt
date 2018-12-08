package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import mu.KotlinLogging
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.BlockChain
import pt.um.lei.masb.blockchain.BlockHeader
import pt.um.lei.masb.blockchain.Coinbase
import pt.um.lei.masb.blockchain.SideChain
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.TransactionOutput
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.Serializable

typealias Loadable<T> = (OElement) -> T?

val logger = KotlinLogging.logger { }

private fun <T> commonLoad(
    document: OElement,
    tName: String,
    loader: Loadable<T>
): T? {
    val name = document.schemaType.map { it.name }.orElse("")
    return if (tName == name) {
        (loader)(document)
    } else {
        logger.error {
            "Got document with class $name"
        }
        null
    }
}

fun loadDummy(
    document: OElement
): DummyData? =
    commonLoad(
        document,
        "Dummy"
    ) { DummyData() }


fun loadHumidity(
    document: OElement
): HumidityData? =
    commonLoad(
        document,
        "Humidity"
    )
    {
        val prop = document.getProperty<Byte>("unit")
        val unit = when (prop) {
            0x00.toByte() -> HUnit.G_BY_KG
            0x01.toByte() -> HUnit.KG_BY_KG
            0x02.toByte() -> HUnit.RELATIVE
            else -> null
        }
        if (unit == null) {
            logger.error {
                "Unit is not one of the expected: $prop"
            }
            null
        } else {
            HumidityData(
                document.getProperty("hum"),
                unit
            )
        }

    }

fun loadLuminosity(
    document: OElement
): LuminosityData? =
    commonLoad(
        document,
        "Luminosity"
    ) {
        val prop = document.getProperty<Byte>("unit")
        val unit = when (prop) {
            0x00.toByte() -> LUnit.LUMENS
            0x01.toByte() -> LUnit.LUX
            else -> null
        }
        if (unit == null) {
            logger.error {
                "Unit is not one of the expected: $prop"
            }
            null
        } else {
            LuminosityData(
                document.getProperty("hum"),
                unit
            )
        }
    }


fun loadNoise(
    document: OElement
): NoiseData? =
    commonLoad(
        document,
        "Noise"
    ) {
        val prop = document.getProperty<Byte>("unit")
        val unit = when (prop) {
            0x00.toByte() -> NUnit.DBSPL
            0x01.toByte() -> NUnit.RMS
            else -> null
        }
        if (unit == null) {
            logger.error {
                "Unit is not one of the expected: $prop"
            }
            null
        } else {
            NoiseData(
                document.getProperty("noiseLevel"),
                document.getProperty("peakOrBase"),
                unit
            )
        }
    }

@Suppress("UNCHECKED_CAST")
fun loadOther(
    document: OElement
): OtherData? =
    commonLoad(
        document,
        "Other"
    ) {
        val bos = ByteArrayOutputStream()
        val chunkIds: List<OIdentifiable> =
            document.getProperty("data")
        for (id in chunkIds) {
            val chunk = id.getRecord<ORecordBytes>()
            chunk.toOutputStream(bos)
            chunk.unload()
        }
        val data: Serializable
        try {
            val oos =
                ObjectInputStream(
                    ByteArrayInputStream(
                        bos.toByteArray()
                    )
                )
            data = oos.readObject() as Serializable
            OtherData(
                data
            )
        } catch (ex: Exception) {
            logger.error(ex) {
                "Error in reading serialized bytes back to Other"
            }
            null
        }
    }

fun loadTemperature(
    document: OElement
): TemperatureData? =
    commonLoad(
        document,
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
            logger.error {
                "Unit is not one of the expected: $prop"
            }
            null
        } else {
            TemperatureData(
                it.getProperty("temperature"),
                unit
            )
        }
    }


fun loadBlock(
    document: OElement
): Block? =
    commonLoad(
        document,
        "Block"
    ) {
        TODO()
    }


fun loadBlockChain(
    document: OElement
): BlockChain? =
    commonLoad(
        document,
        "BlockChain"
    ) {
        TODO()
    }


fun loadBlockHeader(
    document: OElement
): BlockHeader? =
    commonLoad(
        document,
        "BlockHeader"
    ) {
        TODO()
    }


fun loadCoinbase(
    document: OElement
): Coinbase? =
    commonLoad(
        document,
        "Coinbase"
    ) {
        TODO()
    }


fun loadSideChain(
    document: OElement
): SideChain? =
    commonLoad(
        document,
        "SideChain"
    ) {
        TODO()
    }


fun loadTransaction(
    document: OElement
): Transaction? =
    commonLoad(
        document,
        "Transaction"
    ) {
        TODO()
    }


fun loadTransactionOutput(
    document: OElement
): TransactionOutput? =
    commonLoad(
        document,
        "TransactionOutput"
    ) {
        TODO()
    }

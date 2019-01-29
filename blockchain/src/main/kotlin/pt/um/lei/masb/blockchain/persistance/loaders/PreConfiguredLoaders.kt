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
            },
            "PollutionAQ" to Loadable {
                commonLoad(
                    it,
                    "PollutionAQ"
                ) {
                    val byteP = it.getProperty<Byte>("parameter")
                    val parameter = when (byteP) {
                        0x00.toByte() -> PollutionAQ.PollutionType.PM25
                        0x01.toByte() -> PollutionAQ.PollutionType.PM10
                        0x02.toByte() -> PollutionAQ.PollutionType.SO2
                        0x03.toByte() -> PollutionAQ.PollutionType.NO2
                        0x04.toByte() -> PollutionAQ.PollutionType.O3
                        0x05.toByte() -> PollutionAQ.PollutionType.CO
                        0x06.toByte() -> PollutionAQ.PollutionType.BC
                        0x07.toByte() -> PollutionAQ.PollutionType.NA
                        else -> null
                    }
                    if (parameter == null) {
                        throw LoadFailedException(
                            "Parameter is not one of the expected: $byteP"
                        )
                    } else {
                        PollutionAQ(
                            it.getProperty("lat"),
                            it.getProperty("lon"),
                            it.getProperty("lastUpdated"),
                            it.getProperty("unit"),
                            parameter,
                            it.getProperty("value"),
                            it.getProperty("sourceName"),
                            it.getProperty("date"),
                            it.getProperty("city"),
                            it.getProperty("citySeqNum")
                        )
                    }
                }
            },
            "PollutionOWM" to Loadable {
                commonLoad(
                    it,
                    "PollutionOWM"
                ) {
                    val byteP = it.getProperty<Byte>("parameter")
                    val parameter = when (byteP) {
                        0x00.toByte() -> PollutionOWM.PollutionType.O3
                        0x01.toByte() -> PollutionOWM.PollutionType.UV
                        0x02.toByte() -> PollutionOWM.PollutionType.CO
                        0x03.toByte() -> PollutionOWM.PollutionType.SO2
                        0x04.toByte() -> PollutionOWM.PollutionType.NO2
                        0x05.toByte() -> PollutionOWM.PollutionType.NA
                        else -> null
                    }
                    if (parameter == null) {
                        throw LoadFailedException(
                            "Parameter is not one of the expected: $byteP"
                        )
                    } else {

                        PollutionOWM(
                            it.getProperty("lat"),
                            it.getProperty("lon"),
                            it.getProperty("date"),
                            it.getProperty("unit"),
                            parameter,
                            it.getProperty("value"),
                            it.getProperty("data"),
                            it.getProperty("city"),
                            it.getProperty("citySeqNum")
                        )
                    }
                }
            },
            "TrafficFlow" to Loadable {
                commonLoad(
                    it,
                    "TrafficFlow"
                ) {
                    TrafficFlow(
                        it.getProperty("trafficLat"),
                        it.getProperty("trafficLon"),
                        it.getProperty("date"),
                        it.getProperty("functionalRoadClass"),
                        it.getProperty("currentSpeed"),
                        it.getProperty("freeFlowSpeed"),
                        it.getProperty("currentTravelTime"),
                        it.getProperty("freeFlowTravelTime"),
                        it.getProperty("confidence"),
                        it.getProperty("realtimeRatio"),
                        it.getProperty("city"),
                        it.getProperty("citySeqNum")
                    )
                }
            },
            "TrafficIncident" to Loadable {
                commonLoad(
                    it,
                    "TrafficIncident"
                ) {
                    TrafficIncident(
                        it.getProperty("trafficLat"),
                        it.getProperty("trafficLon"),
                        it.getProperty("date"),
                        it.getProperty("trafficModelId"),
                        it.getProperty("id"),
                        it.getProperty("iconLat"),
                        it.getProperty("iconLon"),
                        it.getProperty("incidentCategory"),
                        it.getProperty("magnitudeOfDelay"),
                        it.getProperty("description"),
                        it.getProperty("causeOfAccident"),
                        it.getProperty("from"),
                        it.getProperty("to"),
                        it.getProperty("length"),
                        it.getProperty("delayInSeconds"),
                        it.getProperty("affectedRoads"),
                        it.getProperty("city"),
                        it.getProperty("citySeqNum")
                    )
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
package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import pt.um.lei.masb.blockchain.data.*
import pt.um.lei.masb.blockchain.persistance.results.DataResult
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
                ) {
                    DataResult.Success(
                        DummyData()
                    )
                }
            },

            "Humidity" to Loadable {
                commonLoad<HumidityData>(
                    it,
                    "Humidity"
                )
                {
                    val prop = it.getProperty<Int>("unit")
                    val unit = when (prop) {
                        HUnit.G_BY_KG.ordinal -> HUnit.G_BY_KG
                        HUnit.KG_BY_KG.ordinal -> HUnit.KG_BY_KG
                        HUnit.RELATIVE.ordinal -> HUnit.RELATIVE
                        else -> null
                    }
                    if (unit == null) {
                        DataResult.UnrecognizedUnit(
                            "HUnit is not one of the expected: $prop"
                        )
                    } else {
                        DataResult.Success(
                            HumidityData(
                                it.getProperty("hum"),
                                unit
                            )
                        )
                    }
                }
            },

            "Luminosity" to Loadable {
                commonLoad<LuminosityData>(
                    it,
                    "Luminosity"
                ) {
                    val prop = it.getProperty<Int>("unit")
                    val unit = when (prop) {
                        LUnit.LUMENS.ordinal -> LUnit.LUMENS
                        LUnit.LUX.ordinal -> LUnit.LUX
                        else -> null
                    }
                    if (unit == null) {
                        DataResult.UnrecognizedUnit(
                            "LUnit is not one of the expected: $prop"
                        )
                    } else {
                        DataResult.Success(
                            LuminosityData(
                                it.getProperty("lum"),
                                unit
                            )
                        )
                    }
                }
            },

            "Noise" to Loadable {
                commonLoad<NoiseData>(
                    it,
                    "Noise"
                ) {
                    val prop = it.getProperty<Int>("unit")
                    val unit = when (prop) {
                        NUnit.DBSPL.ordinal -> NUnit.DBSPL
                        NUnit.RMS.ordinal -> NUnit.RMS
                        else -> null
                    }
                    if (unit == null) {
                        DataResult.UnrecognizedUnit(
                            "Unit is not one of the expected: $prop"
                        )
                    } else {
                        DataResult.Success(
                            NoiseData(
                                it.getProperty("noiseLevel"),
                                it.getProperty("peakOrBase"),
                                unit
                            )
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
                        DataResult.Success(
                            OtherData(
                                it.readObject() as Serializable
                            )
                        )
                    }
                }
            },

            "Temperature" to Loadable {
                commonLoad<TemperatureData>(
                    it,
                    "Temperature"
                ) {
                    val prop = it.getProperty<Int>("unit")
                    val unit = when (prop) {
                        TUnit.CELSIUS.ordinal -> TUnit.CELSIUS
                        TUnit.FAHRENHEIT.ordinal -> TUnit.FAHRENHEIT
                        TUnit.KELVIN.ordinal -> TUnit.KELVIN
                        TUnit.RANKINE.ordinal -> TUnit.RANKINE
                        else -> null
                    }
                    if (unit == null) {
                        DataResult.UnrecognizedUnit(
                            "TUnit is not one of the expected: $prop"
                        )
                    } else {
                        DataResult.Success(
                            TemperatureData(
                                it.getProperty("temperature"),
                                unit
                            )
                        )
                    }
                }
            },
            "PollutionAQ" to Loadable {
                commonLoad<PollutionAQ>(
                    it,
                    "PollutionAQ"
                ) {
                    val byteP = it.getProperty<Int>("parameter")
                    val parameter = when (byteP) {
                        PollutionType.PM25.ordinal -> PollutionType.PM25
                        PollutionType.PM10.ordinal -> PollutionType.PM10
                        PollutionType.SO2.ordinal -> PollutionType.SO2
                        PollutionType.NO2.ordinal -> PollutionType.NO2
                        PollutionType.O3.ordinal -> PollutionType.O3
                        PollutionType.CO.ordinal -> PollutionType.CO
                        PollutionType.BC.ordinal -> PollutionType.BC
                        PollutionType.NA.ordinal -> PollutionType.NA
                        else -> null
                    }
                    if (parameter == null) {
                        DataResult.UnrecognizedUnit(
                            "Parameter is not one of the expected: $byteP"
                        )
                    } else {
                        DataResult.Success(
                            PollutionAQ(
                                it.getProperty("lastUpdated"),
                                it.getProperty("unit"),
                                parameter,
                                it.getProperty("value"),
                                it.getProperty("sourceName"),
                                it.getProperty("city"),
                                it.getProperty("citySeqNum")
                            )
                        )
                    }
                }
            },
            "PollutionOWM" to Loadable {
                commonLoad<PollutionOWM>(
                    it,
                    "PollutionOWM"
                ) {
                    val byteP = it.getProperty<Int>("parameter")
                    val parameter = when (byteP) {
                        PollutionType.O3.ordinal -> PollutionType.O3
                        PollutionType.UV.ordinal -> PollutionType.UV
                        PollutionType.CO.ordinal -> PollutionType.CO
                        PollutionType.SO2.ordinal -> PollutionType.SO2
                        PollutionType.NO2.ordinal -> PollutionType.NO2
                        PollutionType.NA.ordinal -> PollutionType.NA
                        else -> null
                    }
                    if (parameter == null) {
                        DataResult.UnrecognizedUnit(
                            "Parameter is not one of the expected: $byteP"
                        )
                    } else {

                        DataResult.Success(
                            PollutionOWM(
                                it.getProperty("unit"),
                                parameter,
                                it.getProperty("value"),
                                it.getProperty("data"),
                                it.getProperty("city"),
                                it.getProperty("citySeqNum")
                            )
                        )
                    }
                }
            },
            "TrafficFlow" to Loadable {
                commonLoad(
                    it,
                    "TrafficFlow"
                ) {
                    DataResult.Success(
                        TrafficFlow(
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
                    )
                }
            },
            "TrafficIncident" to Loadable {
                commonLoad(
                    it,
                    "TrafficIncident"
                ) {
                    DataResult.Success(
                        TrafficIncident(
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
                    )
                }
            }
        )
    }

    private fun <T : BlockChainData> commonLoad(
        document: OElement,
        tName: String,
        loader: () -> DataResult<T>
    ): DataResult<T> {
        return try {
            val opt = document.schemaType.map { it.name }
            if (opt.isPresent) {
                val name = opt.get()
                if (tName == name) {
                    loader()
                } else {
                    DataResult.UnexpectedClass(
                        "Got document with unexpected class: $name"
                    )
                }
            } else {
                DataResult.NonRegisteredSchema(
                    "Schema not existent for: ${document.toJSON()}"
                )
            }
        } catch (e: Exception) {
            DataResult.QueryFailure(
                e.message ?: "", e
            )
        }
    }

}
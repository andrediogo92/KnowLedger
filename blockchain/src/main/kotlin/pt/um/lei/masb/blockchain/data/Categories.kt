package pt.um.lei.masb.blockchain.data

/**
 * Sensor data categories.
 * <pw>
 * Noise, temperature, humidity and luminosity are first-class.
 * <pw>
 * An everything else class in Other is provided as a object blob.
 */
class Categories {
    companion object {
        val categories: MutableMap<String, List<Class<out Any>>> =
                mutableMapOf("Noise" to mutableListOf(NoiseData::class.java),
                             "Temperature" to mutableListOf(TemperatureData::class.java),
                             "Humidity" to mutableListOf(HumidityData::class.java),
                             "Luminosity" to mutableListOf(LuminosityData::class.java),
                             "Other" to mutableListOf(OtherData::class.java))
    }
}

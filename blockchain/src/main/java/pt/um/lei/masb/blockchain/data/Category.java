package pt.um.lei.masb.blockchain.data;

import com.google.gson.annotations.SerializedName;

/**
 * Sensor data category.
 * <p>
 * Noise, temperature, humidity and luminosity are first-class.
 * <p>
 * Everything else is in Other as a object blob.
 */
public enum Category {
    @SerializedName("Noise")
    NOISE,
    @SerializedName("Temperature")
    TEMPERATURE,
    @SerializedName("Humidity")
    HUMIDITY,
    @SerializedName("Luminosity")
    LUMINOSITY,
    @SerializedName("Other")
    OTHER
}

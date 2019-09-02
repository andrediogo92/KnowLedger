@file:UseSerializers(BigDecimalSerializer::class)

package org.knowledger.ledger.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.BigDecimalSerializer
import java.math.BigDecimal

/**
 * GeoCoords represent coordinates with optional altitude
 * defaulted to sea level or altitude 0.
 */
@Serializable
@SerialName("GeoCoordinates")
data class GeoCoords(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val altitude: BigDecimal = BigDecimal.ZERO
)

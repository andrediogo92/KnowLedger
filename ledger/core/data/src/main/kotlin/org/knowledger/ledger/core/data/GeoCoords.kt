package org.knowledger.ledger.core.data

import java.math.BigDecimal

/**
 * GeoCoords represent coordinates with optional altitude
 * defaulted to sea level or altitude 0.
 */
data class GeoCoords(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val altitude: BigDecimal = BigDecimal.ZERO
)

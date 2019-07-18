package org.knowledger.ledger.data

import org.knowledger.ledger.core.data.PhysicalUnit

/**
 * Two luminosity units considered:
 *
 * Lumens represent raw potency of a light source.
 *
 * Lux represent the average light per area unit.
 */
enum class LUnit : PhysicalUnit {
    LUMENS,
    LUX
}

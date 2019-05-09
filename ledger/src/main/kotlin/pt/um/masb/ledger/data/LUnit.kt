package pt.um.masb.ledger.data

import pt.um.masb.common.data.PhysicalUnit

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

package org.knowledger.ledger.data

import org.knowledger.ledger.core.data.PhysicalUnit
import org.knowledger.ledger.data.NoiseUnit.Rms
import org.knowledger.ledger.data.NoiseUnit.dBSPL

/**
 * Noise units considered:
 *
 * [dBSPL] is a relative dB reading to the standard base considered
 * for dB readings (20 micro Pa).
 *
 * [Rms] is a relative reading to the sound source's muted value in the [-1, 1]
 * interval sourced from a PCM signal.
 */
enum class NoiseUnit : PhysicalUnit {
    Rms,
    @Suppress("EnumEntryName")
    dBSPL
}

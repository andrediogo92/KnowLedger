package pt.um.lei.masb.blockchain.data

/**
 * Noise unit considered:
 * <pw>
 * DBSPL is a relative dB reading to the standard base considered
 * for dB readings (20 micro Pa).
 * <pw>
 * RMS is a relative reading to the sound source's muted value in the [-1, 1]
 * interval sourced from a PCM signal.
 */
enum class NUnit : PhysicalUnit {
    RMS,
    DBSPL
}

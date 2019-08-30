package org.knowledger.ledger.core.misc

import java.time.Instant
import java.time.temporal.ChronoUnit

@Suppress("NOTHING_TO_INLINE")
inline fun Instant.secondsFrom(instant: Instant): Long =
    until(instant, ChronoUnit.SECONDS)

@Suppress("NOTHING_TO_INLINE")
inline fun Instant.secondsFromNow(): Long = secondsFrom(Instant.now())
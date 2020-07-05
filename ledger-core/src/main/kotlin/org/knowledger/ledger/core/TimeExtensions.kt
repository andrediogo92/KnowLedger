package org.knowledger.ledger.core

import java.time.Instant
import java.time.temporal.ChronoUnit

fun Instant.secondsFrom(instant: Instant): Long =
    until(instant, ChronoUnit.SECONDS)

fun Instant.secondsFromNow(): Long = secondsFrom(Instant.now())
package org.knowledger.ledger.core

import kotlinx.datetime.Clock

fun nowUTC(): Instant = Clock.System.now()

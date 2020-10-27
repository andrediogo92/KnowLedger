package org.knowledger.ledger.chain.solver.trackers

import org.knowledger.ledger.database.StorageElement

internal data class ResultTracker(
    val elements: Iterator<StorageElement>,
)
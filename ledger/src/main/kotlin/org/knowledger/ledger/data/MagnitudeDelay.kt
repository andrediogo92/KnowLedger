package org.knowledger.ledger.data

import org.knowledger.ledger.core.base.data.PhysicalUnit

enum class MagnitudeDelay : PhysicalUnit {
    /**
     * Shown as grey on traffic tiles.
     */
    UnknownDelay,
    /**
     * Shown as orange on traffic tiles
     */
    Minor,
    /**
     * Shown as light red on traffic tiles.
     */
    Moderate,
    /**
     * Shown as dark red on traffic tiles.
     */
    Major,
    /**
     * Used for road closures and other indefinite delays:
     * Shown as grey on traffic tiles.
     */
    Undefined
}
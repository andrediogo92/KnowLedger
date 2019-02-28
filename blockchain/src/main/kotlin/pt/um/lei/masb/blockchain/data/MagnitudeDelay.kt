package pt.um.lei.masb.blockchain.data

enum class MagnitudeDelay {
    /**
     * Shown as grey on traffic tiles.
     */
    UnknownDelay {
        override fun toString(): String = "Unknown Delay"
    },
    /**
     * Shown as orange on traffic tiles
     */
    Minor {
        override fun toString(): String = "Minor"
    },
    /**
     * Shown as light red on traffic tiles.
     */
    Moderate {
        override fun toString(): String = "Moderate"
    },
    /**
     * Shown as dark red on traffic tiles.
     */
    Major {
        override fun toString(): String = "Major"
    },
    /**
     * Used for road closures and other indefinite delays:
     * Shown as grey on traffic tiles.
     */
    Undefined {
        override fun toString(): String = "Undefined"
    }

}
package pt.um.masb.ledger.data

enum class PollutionType {
    PM25 {
        override fun toString(): String =
            "Particulate Matter 2.5"
    },
    PM10 {
        override fun toString(): String =
            "Particulate Matter 10"
    },
    BC {
        override fun toString(): String {
            return "Black Carbon"
        }
    },
    O3 {
        override fun toString(): String =
            "Ozone"
    },
    UV {
        override fun toString(): String =
            "Ultraviolet"
    },
    CO {
        override fun toString(): String =
            "Carbon Monoxide"
    },
    SO2 {
        override fun toString(): String =
            "Sulfur Dioxide"
    },
    NO2 {
        override fun toString(): String =
            "Nitrogen Dioxide"
    },
    NA {
        override fun toString(): String =
            "Non Available"
    }


}
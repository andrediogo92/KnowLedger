package org.knowledger.ledger.data

enum class IncidentCategory {
    //Incident Category Constants
    Unknown {
        override fun toString(): String = "Unknown Incident"
    },
    Accident {
        override fun toString(): String = "Accident"
    },
    Fog {
        override fun toString(): String = "Fog"
    },
    DangerousConditions {
        override fun toString(): String = "Dangerous Conditions"
    },
    Rain {
        override fun toString(): String = "Rain"
    },
    Ice {
        override fun toString(): String = "Ice"
    },
    Jam {
        override fun toString(): String = "Jam"
    },
    LaneClosed {
        override fun toString(): String = "Lane Closed"
    },
    RoadClosed {
        override fun toString(): String = "Road Closed"
    },
    RoadWorks {
        override fun toString(): String = "Road Works"
    },
    Wind {
        override fun toString(): String = "Wind"
    },
    Flooding {
        override fun toString(): String = "Flooding"
    },
    Detour {
        override fun toString(): String = "Detour"
    },
    Cluster {
        override fun toString(): String = "Cluster"
    }
}
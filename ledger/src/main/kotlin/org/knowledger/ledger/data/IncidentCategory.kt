package org.knowledger.ledger.data

import org.knowledger.ledger.core.base.data.PhysicalUnit

enum class IncidentCategory : PhysicalUnit {
    //Incident Category Constants
    Unknown,
    Accident,
    Fog,
    DangerousConditions,
    Rain,
    Ice,
    Jam,
    LaneClosed,
    RoadClosed,
    RoadWorks,
    Wind,
    Flooding,
    Detour,
    Cluster
}
package org.knowledger.ledger.data

import org.knowledger.ledger.core.data.PhysicalUnit

enum class PollutionType(
    val description: String
) : PhysicalUnit {
    PM25("Particulate Matter 2.5"),
    PM10("Particulate Matter 10"),
    BC("Black Carbon"),
    O3("Ozone"),
    UV("Ultraviolet"),
    CO("Carbon Monoxide"),
    SO2("Sulfur Dioxide"),
    NO2("Nitrogen Dioxide"),
    NA("Non-Available")
}
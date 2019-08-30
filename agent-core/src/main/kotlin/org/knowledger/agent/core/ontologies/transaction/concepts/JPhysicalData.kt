package org.knowledger.agent.core.ontologies.transaction.concepts

import jade.content.Concept

data class JPhysicalData(
    var data: String,
    var seconds: Long,
    var nanos: Int,
    var latitude: String,
    var longitude: String,
    var altitude: String
) : Concept, Comparable<JPhysicalData> {
    override fun compareTo(other: JPhysicalData): Int =
        when {
            seconds > other.seconds -> -1
            seconds < other.seconds -> 1
            else -> when {
                nanos > other.nanos -> -1
                nanos < other.nanos -> 1
                else -> 0
            }
        }

}

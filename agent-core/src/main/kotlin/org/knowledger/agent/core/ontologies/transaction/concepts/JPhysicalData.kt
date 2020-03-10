package org.knowledger.agent.core.ontologies.transaction.concepts

import jade.content.Concept
import org.knowledger.base64.Base64String

data class JPhysicalData(
    var data: Base64String,
    var tag: JHash,
    var millis: Long,
    var latitude: String,
    var longitude: String,
    var altitude: String
) : Concept, Comparable<JPhysicalData> {
    override fun compareTo(other: JPhysicalData): Int =
        millis.compareTo(other.millis)

}

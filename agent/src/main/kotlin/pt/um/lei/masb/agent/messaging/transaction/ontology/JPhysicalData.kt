package pt.um.lei.masb.agent.messaging.transaction.ontology

import jade.content.Concept

data class JPhysicalData(
    val data: String,
    val instant: String,
    val lat: String,
    val lng: String
) : Concept

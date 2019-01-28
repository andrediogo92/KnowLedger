package pt.um.lei.masb.agent.messaging.transaction.ontology.concepts

import jade.content.Concept

data class JPhysicalData(
    var data: String,
    var instant: String,
    var lat: String,
    var lng: String
) : Concept

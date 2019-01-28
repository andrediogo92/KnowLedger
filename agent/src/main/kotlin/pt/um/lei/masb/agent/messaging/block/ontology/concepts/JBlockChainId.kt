package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JBlockChainId(
    var uuid: String,
    var timestamp: String,
    var id: String
) : Concept

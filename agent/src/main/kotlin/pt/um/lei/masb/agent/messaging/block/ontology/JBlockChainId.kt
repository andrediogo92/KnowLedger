package pt.um.lei.masb.agent.messaging.block.ontology

import jade.content.Concept

data class JBlockChainId(
    val uuid: String,
    val timestamp: String,
    val id: String
) : Concept

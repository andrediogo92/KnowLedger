package pt.um.lei.masb.agent.messaging.block.ontology.actions

import jade.content.AgentAction

data class QueryBlockHeaderByHeight(
    var blockheight: Long
) : AgentAction
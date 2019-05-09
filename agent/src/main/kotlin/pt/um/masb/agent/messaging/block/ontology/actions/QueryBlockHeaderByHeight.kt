package pt.um.masb.agent.messaging.block.ontology.actions

import pt.um.masb.agent.messaging.Actionable


data class QueryBlockHeaderByHeight(
    var blockheight: Long
) : Actionable
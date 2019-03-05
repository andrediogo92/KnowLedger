package pt.um.lei.masb.agent.messaging.block.ontology.actions

import pt.um.lei.masb.agent.messaging.Actionable

data class QueryBlockHeaderByHeight(
    var blockheight: Long
) : Actionable
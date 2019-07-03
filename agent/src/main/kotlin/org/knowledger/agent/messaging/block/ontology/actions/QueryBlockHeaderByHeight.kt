package org.knowledger.agent.messaging.block.ontology.actions

import org.knowledger.agent.messaging.Actionable


data class QueryBlockHeaderByHeight(
    var blockheight: Long
) : Actionable
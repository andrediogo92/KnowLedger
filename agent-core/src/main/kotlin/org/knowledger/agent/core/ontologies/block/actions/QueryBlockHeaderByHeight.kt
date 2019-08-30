package org.knowledger.agent.core.ontologies.block.actions

import jade.content.AgentAction


data class QueryBlockHeaderByHeight(
    var blockheight: Long
) : AgentAction
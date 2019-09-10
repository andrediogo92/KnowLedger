package org.knowledger.agent.core.ontologies.block.actions

import jade.content.AgentAction


data class RequestBlocksFrom(
    val from: Long
) : AgentAction
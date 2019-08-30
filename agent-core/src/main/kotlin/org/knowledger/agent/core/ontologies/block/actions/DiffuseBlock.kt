package org.knowledger.agent.core.ontologies.block.actions

import jade.content.AgentAction
import jade.core.AID
import org.knowledger.agent.core.ontologies.block.concepts.JBlock

data class DiffuseBlock(
    var block: JBlock,
    var AID: AID
) : AgentAction
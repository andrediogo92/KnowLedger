package org.knowledger.agent.core.ontologies.block.actions

import jade.content.AgentAction
import jade.core.AID
import org.knowledger.agent.core.ontologies.block.concepts.JBlockHeader

data class DiffuseBlockHeader(
    var blockHeader: JBlockHeader,
    var AID: AID
) : AgentAction
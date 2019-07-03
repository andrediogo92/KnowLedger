package org.knowledger.agent.messaging.block.ontology.actions

import jade.core.AID
import org.knowledger.agent.messaging.Actionable
import org.knowledger.agent.messaging.block.ontology.concepts.JBlock

data class DiffuseBlock(
    var block: JBlock,
    var AID: AID
) : Actionable
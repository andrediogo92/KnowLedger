package org.knowledger.agent.messaging.block.ontology.actions

import jade.core.AID
import org.knowledger.agent.messaging.Actionable
import org.knowledger.agent.messaging.block.ontology.concepts.JBlockHeader

data class DiffuseBlockHeader(
    var blockHeader: JBlockHeader,
    var AID: AID
) : Actionable
package pt.um.lei.masb.agent.messaging.block.ontology.actions

import jade.content.AgentAction
import jade.core.AID
import pt.um.lei.masb.agent.messaging.block.ontology.concepts.JBlockHeader

data class DiffuseBlockHeader(
    var blockHeader: JBlockHeader,
    var AID: AID
) : AgentAction
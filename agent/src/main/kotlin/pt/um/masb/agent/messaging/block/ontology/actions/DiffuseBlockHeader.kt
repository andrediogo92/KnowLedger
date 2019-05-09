package pt.um.masb.agent.messaging.block.ontology.actions

import jade.core.AID
import pt.um.masb.agent.messaging.Actionable
import pt.um.masb.agent.messaging.block.ontology.concepts.JBlockHeader

data class DiffuseBlockHeader(
    var blockHeader: JBlockHeader,
    var AID: AID
) : Actionable
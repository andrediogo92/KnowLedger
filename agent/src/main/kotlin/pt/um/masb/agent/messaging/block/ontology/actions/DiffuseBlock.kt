package pt.um.masb.agent.messaging.block.ontology.actions

import jade.core.AID
import pt.um.masb.agent.messaging.Actionable
import pt.um.masb.agent.messaging.block.ontology.concepts.JBlock

data class DiffuseBlock(
    var block: JBlock,
    var AID: AID
) : Actionable
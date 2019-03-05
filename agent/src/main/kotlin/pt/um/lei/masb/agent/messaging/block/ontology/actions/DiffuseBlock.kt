package pt.um.lei.masb.agent.messaging.block.ontology.actions

import jade.core.AID
import pt.um.lei.masb.agent.messaging.Actionable
import pt.um.lei.masb.agent.messaging.block.ontology.concepts.JBlock

data class DiffuseBlock(
    var block: JBlock,
    var AID: AID
) : Actionable
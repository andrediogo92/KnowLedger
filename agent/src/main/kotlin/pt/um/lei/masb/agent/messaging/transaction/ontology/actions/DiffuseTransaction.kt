package pt.um.lei.masb.agent.messaging.transaction.ontology.actions

import jade.content.AgentAction
import pt.um.lei.masb.agent.messaging.transaction.ontology.concepts.JTransaction

data class DiffuseTransaction(
    var transaction: JTransaction
) : AgentAction
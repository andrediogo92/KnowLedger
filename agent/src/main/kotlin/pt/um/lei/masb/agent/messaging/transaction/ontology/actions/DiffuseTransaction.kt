package pt.um.lei.masb.agent.messaging.transaction.ontology.actions

import pt.um.lei.masb.agent.messaging.Actionable
import pt.um.lei.masb.agent.messaging.transaction.ontology.concepts.JTransaction

data class DiffuseTransaction(
    var transaction: JTransaction
) : Actionable
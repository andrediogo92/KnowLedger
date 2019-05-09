package pt.um.masb.agent.messaging.transaction.ontology.actions

import pt.um.masb.agent.messaging.Actionable
import pt.um.masb.agent.messaging.transaction.ontology.concepts.JTransaction


data class DiffuseTransaction(
    var transaction: JTransaction
) : Actionable
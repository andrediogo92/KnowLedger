package org.knowledger.agent.messaging.transaction.ontology.actions

import org.knowledger.agent.messaging.Actionable
import org.knowledger.agent.messaging.transaction.ontology.concepts.JTransaction


data class DiffuseTransaction(
    var transaction: JTransaction
) : Actionable
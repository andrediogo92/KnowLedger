package org.knowledger.agent.messaging.ledger.ontology.concepts

import jade.content.Concept

data class JCoinbaseParams(
    var timeIncentive: Long,
    var valueIncentive: Long,
    var baseIncentive: Long,
    var dividingThreshold: Long
) : Concept
package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept

data class JCoinbaseParams(
    var timeIncentive: Long,
    var valueIncentive: Long,
    var baseIncentive: Long,
    var dividingThreshold: Long
) : Concept
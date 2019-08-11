package org.knowledger.agent.messaging.ledger.ontology.concepts

import jade.content.Concept

data class JLedgerParams(
    var crypter: String,
    var recalcTime: Long,
    var recalcTrigger: Long,
    var blockParams: JBlockParams
) : Concept
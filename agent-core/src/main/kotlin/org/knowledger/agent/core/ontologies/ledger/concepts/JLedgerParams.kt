package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept

data class JLedgerParams(
    var crypter: String,
    var recalcTime: Long,
    var recalcTrigger: Long,
    var blockParams: JBlockParams
) : Concept
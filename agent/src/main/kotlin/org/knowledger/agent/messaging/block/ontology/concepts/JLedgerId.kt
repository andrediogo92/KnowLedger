package org.knowledger.agent.messaging.block.ontology.concepts

import jade.content.Concept
import org.knowledger.ledger.config.LedgerParams

data class JLedgerId(
    var uuid: String,
    var timestamp: String,
    val params: LedgerParams,
    var id: String,
    val hash: String
) : Concept

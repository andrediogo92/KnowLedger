package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept
import org.knowledger.base64.Base64String

data class JLedgerParams(
    var hasher: Base64String,
    var recalcTime: Long,
    var recalcTrigger: Long,
    var blockParams: JBlockParams
) : Concept
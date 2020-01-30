package org.knowledger.agent.core.ontologies.ledger.concepts

import jade.content.Concept
import org.knowledger.base64.Base64String

data class JLedgerParams(
    var hasher: Base64String,
    var recalculationTime: Long,
    var recalculationTrigger: Int,
    var blockParams: JBlockParams
) : Concept
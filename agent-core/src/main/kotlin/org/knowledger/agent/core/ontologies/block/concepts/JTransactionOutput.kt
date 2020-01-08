package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash
import org.knowledger.base64.Base64String

data class JTransactionOutput(
    var pubkey: Base64String,
    var prevCoinbase: JHash,
    var hashId: JHash,
    var payout: String,
    var tx: Set<JHash>
) : Concept

package org.knowledger.agent.messaging.block.ontology.concepts

import jade.content.Concept
import org.knowledger.agent.messaging.ledger.ontology.concepts.JBlockParams
import org.knowledger.agent.messaging.ledger.ontology.concepts.JChainId

data class JBlockHeader(
    var chainId: JChainId,
    var hash: String,
    var merkleRoot: String,
    var previousHash: String,
    var params: JBlockParams,
    var seconds: Long,
    var nonce: Long
) : Concept

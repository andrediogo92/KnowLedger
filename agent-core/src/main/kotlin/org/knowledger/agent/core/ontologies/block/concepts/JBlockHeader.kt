package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.ledger.concepts.JBlockParams
import org.knowledger.agent.core.ontologies.ledger.concepts.JChainId

data class JBlockHeader(
    var chainId: JChainId,
    var hash: String,
    var merkleRoot: String,
    var previousHash: String,
    var params: JBlockParams,
    var seconds: Long,
    var nonce: Long
) : Concept

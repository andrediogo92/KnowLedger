package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.ledger.concepts.JBlockParams
import org.knowledger.agent.core.ontologies.ledger.concepts.JChainId
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash

data class JBlockHeader(
    var chainId: JChainId,
    var hash: JHash,
    var merkleRoot: JHash,
    var previousHash: JHash,
    var params: JBlockParams,
    var seconds: Long,
    var nonce: Long
) : Concept

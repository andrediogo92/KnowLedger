package org.knowledger.agent.core.ontologies.block.concepts

import jade.content.Concept
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash
import org.knowledger.collections.MutableSortedList

data class JCoinbase(
    var witnesses: MutableSortedList<JWitness>,
    var payout: String,
    var difficulty: JHash,
    var blockheight: Long,
    var extraNonce: Long,
    var hashId: JHash,
    var formula: JHash?
) : Concept
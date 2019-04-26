package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept
import pt.um.lei.masb.blockchain.ledger.config.LedgerParams

data class JLedgerId(
    var uuid: String,
    var timestamp: String,
    val params: LedgerParams,
    var id: String,
    val hash: String
) : Concept

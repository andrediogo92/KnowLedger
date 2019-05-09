package pt.um.masb.agent.data.apis

import pt.um.masb.ledger.Transaction


interface ApiIterator : Iterator<Transaction> {

    val transactions: Collection<Transaction>
}
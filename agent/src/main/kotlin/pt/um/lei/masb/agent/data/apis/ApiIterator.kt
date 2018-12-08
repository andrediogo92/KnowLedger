package pt.um.lei.masb.agent.data.apis

import pt.um.lei.masb.blockchain.Transaction

interface ApiIterator : Iterator<Transaction> {

    val transactions: Collection<Transaction>
}
package pt.um.lei.masb.agent.data.feed

import pt.um.lei.masb.blockchain.data.BlockChainData

interface Reduxer {
    fun reduce(type: BlockChainData): String
    fun type(): String
}

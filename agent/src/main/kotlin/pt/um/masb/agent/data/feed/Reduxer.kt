package pt.um.masb.agent.data.feed

import pt.um.masb.common.data.BlockChainData


interface Reduxer {
    fun reduce(type: BlockChainData): String
    fun type(): String
}

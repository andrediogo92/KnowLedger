package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept
import pt.um.lei.masb.blockchain.ledger.BlockParams

data class JBlockHeader(
    var blid: String,
    var difficulty: String,
    var blockheight: Long,
    var hash: String,
    var merkleRoot: String,
    var previousHash: String,
    var params: BlockParams,
    var timeStamp: String,
    var nonce: Long
) : Concept {
    override fun toString(): String {
        return "JBlockHeader(blid='$blid', difficulty='$difficulty', blockheight=$blockheight, hash='$hash', merkleRoot='$merkleRoot', previousHash='$previousHash', params=$params, timeStamp='$timeStamp', nonce=$nonce)"
    }
}

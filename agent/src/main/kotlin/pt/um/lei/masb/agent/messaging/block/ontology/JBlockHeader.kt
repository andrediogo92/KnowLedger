package pt.um.lei.masb.agent.messaging.block.ontology

import jade.content.Concept

data class JBlockHeader(
    val blid: JBlockChainId,
    val difficulty: String,
    val blockheight: Long,
    val hash: String,
    val merkleRoot: String,
    val previousHash: String,
    val timeStamp: String,
    val nonce: Long
) : Concept {

    override fun toString(): String {
        return "JBlockHeader{" +
                "difficulty='" + difficulty + '\''.toString() +
                ", blockheight=" + blockheight +
                ", hash='" + hash + '\''.toString() +
                ", _merkleRoot='" + merkleRoot + '\''.toString() +
                ", previousHash='" + previousHash + '\''.toString() +
                ", timeStamp=" + timeStamp +
                ", nonce=" + nonce +
                '}'.toString()
    }
}

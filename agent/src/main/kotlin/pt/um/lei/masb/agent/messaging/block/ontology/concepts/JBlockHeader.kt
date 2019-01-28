package pt.um.lei.masb.agent.messaging.block.ontology.concepts

import jade.content.Concept

data class JBlockHeader(
    var blid: String,
    var difficulty: String,
    var blockheight: Long,
    var hash: String,
    var merkleRoot: String,
    var previousHash: String,
    var timeStamp: String,
    var nonce: Long
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

package pt.um.lei.masb.agent.behaviours

import jade.core.behaviours.Behaviour
import mu.KLogging
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.SideChain
import java.util.*


class Mining(
    private val sc: SideChain,
    private val blockQueue: Queue<Block>
) : Behaviour() {
    private var block: Block? = null

    override fun action() {
        var mining = true
        //just for debugging
        while (mining) {
            if (block == null) {
                if (blockQueue.isEmpty()) {
                    mining = false
                } else {
                    block = blockQueue.remove()
                    mining = !block!!.attemptMineBlock(
                        invalidate = false,
                        time = false
                    )
                }
            } else {
                mining = !block!!.attemptMineBlock(
                    invalidate = false,
                    time = false
                )
            }
        }
    }


    override fun done(): Boolean {
        if (block != null) {
            logger.debug {
                "Finished mining ${block!!.header.blockheight} -> ${block!!.header.merkleRoot}"
            }
            sc.addBlock(block!!)
            block = null
        } else {
            logger.debug { "No block" }
        }
        return false
    }

    companion object : KLogging()
}

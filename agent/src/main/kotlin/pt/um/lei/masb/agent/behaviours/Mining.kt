package pt.um.lei.masb.agent.behaviours

import jade.core.behaviours.Behaviour
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.SideChain
import java.util.*


class Mining(
    private val bc: SideChain,
    private val blockQueue: Queue<Block>
) : Behaviour() {
    private var block: Block? = null
    private var mining: Boolean = false

    init {
        mining = true
    }

    override fun action() {
        //just for debugging
        while (mining) {
            if (block == null) {
                if (blockQueue.isEmpty()) {
                    mining = false
                } else {
                    block = blockQueue.remove()
                    mining = !block!!.attemptMineBlock(false, false)
                }
            } else {
                mining = !block!!.attemptMineBlock(false, false)
            }
        }
        block = null
    }


    override fun done(): Boolean {
        println("Finished mining")
        return true
    }
}

package org.knowledger.agent.behaviours

import jade.core.behaviours.Behaviour
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.storage.Block
import org.tinylog.kotlin.Logger


class Mining(
    private val sc: ChainHandle
) : Behaviour() {
    private var block: Block? = null
    private val mined: Boolean = false

    override fun action() {
        var mining = true
        //just for debugging
        /**
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
         */
    }


    override fun done(): Boolean {
        if (block != null) {
            Logger.debug {
                "Finished mining ${block!!.header.blockheight} -> ${block!!.header.merkleRoot}"
            }
            sc.addBlock(block!!)
            block = null
        } else {
            Logger.debug { "No block" }
        }
        return mined
    }
}

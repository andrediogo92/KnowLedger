package pt.um.lei.masb.agent.behaviours

import jade.core.behaviours.Behaviour
import mu.KLogging
import pt.um.lei.masb.agent.data.captureSound
import pt.um.lei.masb.blockchain.data.NUnit
import pt.um.lei.masb.blockchain.data.NoiseData
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.ledger.Block
import pt.um.lei.masb.blockchain.ledger.Transaction
import pt.um.lei.masb.blockchain.service.ChainHandle
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.blockchain.utils.RingBuffer
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class CaptureData(
    private val sc: ChainHandle,
    private val id: Ident,
    private val blockQueue: Queue<Block>,
    private val toSend: RingBuffer<Transaction>
) : Behaviour() {

    override fun action() {
        if (blockQueue.isEmpty()) {
            //Generate new block
            val block = sc.newBlock()
            if (block != null) {
                blockQueue.offer(block)
            }
        }

        val currentBlock = blockQueue.peek()


        val sc = captureSound()

        if (sc != null) {
            val noise = NoiseData(
                sc.first,
                sc.second,
                NUnit.RMS
            )
            logger.info {
                "$noise"
            }
            val sd = PhysicalData(
                Instant.now(),
                BigDecimal(41.5449583),
                BigDecimal(-8.4257831),
                noise
            )

            val t = Transaction(id, sd)

            currentBlock.addTransaction(t)

            toSend.offer(t)
        }
    }


    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()

}
package pt.um.lei.masb.agent.behaviours

import jade.core.behaviours.Behaviour
import mu.KLogging
import pt.um.lei.masb.agent.data.captureSound
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.Ident
import pt.um.lei.masb.blockchain.SideChain
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.data.NUnit
import pt.um.lei.masb.blockchain.data.NoiseData
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.utils.RingBuffer
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class CaptureData(
    private val sc: SideChain,
    private val id: Ident,
    private val blockQueue: Queue<Block>,
    private val toSend: RingBuffer<Transaction>
) : Behaviour() {

    override fun action() {
        if (blockQueue.isEmpty()) {
            /**
             * TODO arrange queuing logic.
             */
        }

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
            /**
             * TODO transaction posting to next block in line.
             *
             * val t = Transaction(,id, sd)
             * toSend.offer(t)
             * blockQueue.add(bl)
             */

        }
    }


    override fun done(): Boolean {
        return true
    }

    companion object : KLogging()

}
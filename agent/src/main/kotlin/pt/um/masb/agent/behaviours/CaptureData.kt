package pt.um.masb.agent.behaviours

import jade.core.behaviours.Behaviour
import mu.KLogging
import pt.um.masb.agent.data.captureSound
import pt.um.masb.ledger.data.NUnit
import pt.um.masb.ledger.data.NoiseData
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.storage.Transaction
import java.math.BigDecimal
import java.time.Instant

class CaptureData(
    private val handle: ChainHandle,
    private val id: Identity,
    private val toSend: MutableList<Transaction>
) : Behaviour() {

    override fun action() {
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

            handle.addTransaction(t)

            toSend.add(t)
        }
    }


    override fun done(): Boolean {
        return false
    }

    companion object : KLogging()

}
package org.knowledger.agent.behaviours

import jade.core.behaviours.Behaviour
import org.knowledger.agent.data.captureSound
import org.knowledger.common.hash.AvailableHashAlgorithms
import org.knowledger.ledger.data.NUnit
import org.knowledger.ledger.data.NoiseData
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.storage.Transaction
import org.tinylog.kotlin.Logger
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
            Logger.info {
                "$noise"
            }
            val sd = PhysicalData(
                Instant.now(),
                BigDecimal(41.5449583),
                BigDecimal(-8.4257831),
                noise
            )

            //Use standard SHA256
            //TODO: Take as parameter.
            val t = Transaction(id, sd, AvailableHashAlgorithms.SHA256Hasher)

            handle.addTransaction(t)

            toSend.add(t)
        }
    }


    override fun done(): Boolean {
        return false
    }

}
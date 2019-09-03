package org.knowledger.example.slave

import jade.core.Agent
import jade.core.behaviours.TickerBehaviour
import org.knowledger.ledger.core.data.PhysicalData
import java.time.Duration
import java.util.concurrent.BlockingQueue

class CaptureSoundData internal constructor(
    val queue: BlockingQueue<PhysicalData>,
    agent: Agent
) : TickerBehaviour(agent, Duration.ofSeconds(30).toMillis()) {
    override fun onTick() {
        val sd = captureSound()

        if (sd != null) {
            queue.put(sd)
        }
    }
}
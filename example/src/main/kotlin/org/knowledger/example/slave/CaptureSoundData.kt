package org.knowledger.example.slave

import jade.core.Agent
import jade.core.behaviours.TickerBehaviour
import org.knowledger.agent.agents.slave.DataManager
import org.knowledger.agent.messaging.checked
import org.knowledger.ledger.crypto.hash.Hash

class CaptureSoundData internal constructor(
    agent: Agent,
    val id: Hash,
    val queue: DataManager
) : TickerBehaviour(agent, 300) {
    override fun onTick() {
        val sd = captureSound()

        if (sd != null) {
            queue.add(sd.checked(id))
        }
    }
}
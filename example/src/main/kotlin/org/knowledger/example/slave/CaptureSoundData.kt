package org.knowledger.example.slave

import jade.core.Agent
import jade.core.behaviours.TickerBehaviour
import org.knowledger.agent.agents.slave.DataManager

class CaptureSoundData internal constructor(
    agent: Agent,
    val queue: DataManager
) : TickerBehaviour(agent, 300) {
    override fun onTick() {
        val sd = captureSound()

        if (sd != null) {
            queue.add(sd)
        }
    }
}
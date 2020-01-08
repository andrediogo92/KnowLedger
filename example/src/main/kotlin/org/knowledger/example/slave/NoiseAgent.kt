package org.knowledger.example.slave

import org.knowledger.agent.agents.slave.SlaveAgent

class NoiseAgent : SlaveAgent() {
    override fun registerBehaviours() {
        addBehaviour(CaptureSoundData(this, dataManager))
    }
}
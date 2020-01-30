package org.knowledger.example.slave

import org.knowledger.agent.agents.slave.SlaveAgent
import org.knowledger.base64.base64DecodedToHash
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers

class NoiseAgent() : SlaveAgent() {
    val hasher: Hashers =
        if (arguments.size > 1) {
            arguments[1] as Hashers
        } else {
            Hashers.DEFAULT_HASHER
        }

    @Suppress("UNCHECKED_CAST")
    val adapters = arguments[0] as Set<AbstractStorageAdapter<*>>

    override fun registerBehaviours() {
        addBehaviour(
            CaptureSoundData(
                this, adapters.first().id.base64DecodedToHash(),
                dataManager
            )
        )
    }
}
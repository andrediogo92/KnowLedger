package org.knowledger.example

import org.knowledger.agent.agents.AgentContainer
import org.knowledger.ledger.data.adapters.NoiseDataStorageAdapter
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.service.handles.LedgerHandle

class ExampleAgentApp {

    companion object {
        @JvmStatic
        fun main() {

            val container = AgentContainer(
                host = "localhost", port = 9888,
                containerName = "LedgerMain", isMainContainer = true,
                hasGUI = true
            )
            val handle =
                LedgerHandle
                    .Builder()
                    .withLedgerIdentity("test")
                    .unwrap()
                    .build()
                    .unwrap()

            container.runLedgerAgent(
                name = "MinerAgent",
                handle = handle,
                knownTypes = setOf(NoiseDataStorageAdapter(handle.hasher)),
                arguments = emptyArray()
            )

            container.runSlaveAgent(
                name = "NoiseSlave",
                classpath = "org.knowledger.example.slave.NoiseAgent",
                knownTypes = setOf(NoiseDataStorageAdapter(handle.hasher)),
                arguments = emptyArray()
            )
        }
    }
}

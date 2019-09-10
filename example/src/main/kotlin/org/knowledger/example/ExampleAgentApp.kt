package org.knowledger.example

import org.knowledger.agent.agents.AgentContainer
import org.knowledger.ledger.core.results.unwrap
import org.knowledger.ledger.data.adapters.NoiseDataStorageAdapter
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
                LedgerHandle.Builder()
                    .withLedgerIdentity("test")
                    .unwrap()
                    .build()
                    .unwrap()

            container.runLedgerAgent(
                "MinerAgent",
                handle,
                setOf(NoiseDataStorageAdapter),
                emptyArray()
            )

        }
    }
}

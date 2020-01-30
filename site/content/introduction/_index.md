+++
title = "Introduction"
weight = 1
sort_by = "weight"
insert_anchor_links = "right"
+++

KnowLedger is a minimal framework for creating and running distributed ledgers that model physical data for use in Smart Cities.
It is comprised of three major components:
1. A ledger library facilitating the generation of any number of independent chains that a ledger reference. Each chain deals with a single type of structured data.
2. An agent library facilitating the creation of ledger agents that advertise and execute a ledger maintenance protocol and slave agents that periodically supply them data.
3. A core agent library containing only the essential types needed for building ledger compatible data for slave agents.


```kotlin
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
```
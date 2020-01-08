package org.knowledger.agent.agents.slave

import org.knowledger.ledger.config.GlobalLedgerConfiguration
import org.knowledger.ledger.data.PhysicalData
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class DataManager(
    val dataQueue: BlockingQueue<PhysicalData> =
        ArrayBlockingQueue(GlobalLedgerConfiguration.CACHE_SIZE)
) : BlockingQueue<PhysicalData> by dataQueue {
}
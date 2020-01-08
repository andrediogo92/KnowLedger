package org.knowledger.agent.agents.slave

import org.knowledger.agent.data.CheckedData
import org.knowledger.ledger.config.GlobalLedgerConfiguration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class DataManager(
    val dataQueue: BlockingQueue<CheckedData> =
        ArrayBlockingQueue(GlobalLedgerConfiguration.CACHE_SIZE)
) : BlockingQueue<CheckedData> by dataQueue {
}
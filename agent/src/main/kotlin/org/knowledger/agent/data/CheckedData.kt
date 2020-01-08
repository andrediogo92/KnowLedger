package org.knowledger.agent.data

import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.data.Tag

data class CheckedData(val tag: Tag, val data: PhysicalData)
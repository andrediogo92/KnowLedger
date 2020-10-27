package org.knowledger.ledger.chain.data

import org.knowledger.ledger.chain.ServiceClass
import org.knowledger.ledger.crypto.Hash

data class WitnessInfo(val hash: Hash, val index: Int, val max: Long) : ServiceClass
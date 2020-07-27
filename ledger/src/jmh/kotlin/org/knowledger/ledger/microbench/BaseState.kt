package org.knowledger.ledger.microbench

import org.knowledger.ledger.storage.Identity
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.RandomDataSchema
import org.knowledger.testing.storage.generateChainId

open class BaseState {
    val id: Array<Identity> = arrayOf(
        Identity("boy"), Identity("wonder")
    )
    val chainId = generateChainId(random.randomHash(), RandomDataSchema())
}
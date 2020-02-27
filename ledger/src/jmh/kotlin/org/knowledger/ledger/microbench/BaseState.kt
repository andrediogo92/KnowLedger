package org.knowledger.ledger.microbench

import org.knowledger.ledger.service.Identity

open class BaseState {
    val id: Array<Identity> = arrayOf(
        Identity("boy"), Identity("wonder")
    )
}
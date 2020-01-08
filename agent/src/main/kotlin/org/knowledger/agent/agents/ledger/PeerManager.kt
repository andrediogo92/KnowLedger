package org.knowledger.agent.agents.ledger

import jade.core.AID
import org.knowledger.agent.data.PeerBook

internal data class PeerManager(
    val ledgerPeers: PeerBook,
    val slavePeers: PeerBook
) {
    constructor(
        id: AID
    ) : this(
        ledgerPeers = PeerBook(id),
        slavePeers = PeerBook(id)
    )
}
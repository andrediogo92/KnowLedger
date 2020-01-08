package org.knowledger.agent.behaviours.slave

import jade.core.Agent
import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import kotlinx.serialization.BinaryFormat
import org.knowledger.agent.agents.AgentManager
import org.knowledger.agent.agents.slave.DataManager
import org.knowledger.agent.behaviours.ledger.prepareMessage
import org.knowledger.agent.core.ontologies.transaction.predicates.DiffuseData
import org.knowledger.agent.data.PeerBook
import org.knowledger.agent.messaging.state.ConversationIds
import org.knowledger.agent.messaging.toJadePhysicalData
import org.knowledger.base64.base64DecodedToHash
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.database.adapters.AbstractStorageAdapter
import org.tinylog.kotlin.Logger

internal class PropagateData(
    agent: Agent,
    peerBook: PeerBook,
    val agentManager: AgentManager,
    val dataManager: DataManager,
    val typeAdapter: AbstractStorageAdapter<out LedgerData>,
    val encoder: BinaryFormat
) : CyclicBehaviour(agent) {
    val tag = typeAdapter.id.base64DecodedToHash()
    val aids = peerBook.peersByTagSupport(tag)

    override fun action() {
        Logger.debug { "Attempting poll for data" }
        dataManager.poll()?.let { data ->
            Logger.debug { "Data obtained: sending to ${aids.joinToString { it.localName }}" }
            prepareMessage(
                aids, ConversationIds.NewData,
                ACLMessage.INFORM,
                DiffuseData(
                    data.toJadePhysicalData(
                        typeAdapter.serializer,
                        encoder,
                        tag
                    )
                )
            )
        } ?: block(500)
    }
}

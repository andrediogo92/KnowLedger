package org.knowledger.agent.behaviours.slave

import jade.core.Agent
import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import kotlinx.serialization.BinaryFormat
import org.knowledger.agent.agents.AgentManager
import org.knowledger.agent.agents.slave.DataManager
import org.knowledger.agent.agents.slaveDebug
import org.knowledger.agent.agents.slaveError
import org.knowledger.agent.behaviours.ledger.prepareMessage
import org.knowledger.agent.core.ontologies.transaction.predicates.DiffuseData
import org.knowledger.agent.data.PeerBook
import org.knowledger.agent.messaging.state.ConversationIds
import org.knowledger.agent.messaging.toJadePhysicalData
import org.knowledger.base64.base64DecodedToHash
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.data.LedgerData

internal class PropagateData(
    agent: Agent,
    val peerBook: PeerBook,
    val agentManager: AgentManager,
    val dataManager: DataManager,
    val typeAdapters: Set<AbstractStorageAdapter<out LedgerData>>,
    val encoder: BinaryFormat
) : CyclicBehaviour(agent) {

    override fun action() {
        agent.slaveDebug { "Attempting poll for data" }
        dataManager.poll()?.let { data ->
            val typeAdapter = typeAdapters.firstOrNull { adapter ->
                adapter.id.base64DecodedToHash() == data.tag
            }
            if (typeAdapter != null) {
                val aids = peerBook.peersByTagSupport(data.tag)
                agent.slaveDebug { "Data obtained: sending to ${aids.joinToString { it.localName }}" }
                prepareMessage(
                    aids, ConversationIds.NewData,
                    ACLMessage.INFORM,
                    DiffuseData(
                        data.data.toJadePhysicalData(
                            typeAdapter.serializer,
                            encoder,
                            data.tag
                        )
                    )
                )
            } else {
                agent.slaveError { "Data polled is not supported: ${data.tag.toHexString()}" }
            }
        } ?: block(500)
    }
}

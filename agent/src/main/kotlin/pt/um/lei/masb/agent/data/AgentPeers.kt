package pt.um.lei.masb.agent.data

import jade.core.AID
import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription

/**
 * Class in charge of bookkeeping of agents identities selected for peering.
 */
data class AgentPeers(
    val agent: Agent,
    val ledgerPeers: MutableList<AID> = mutableListOf()
) {
    init {
        if (ledgerPeers.isEmpty()) {
            val dfd = DFAgentDescription()
            val agents = DFService.search(agent, dfd)
            agents.asSequence().map { it.name }.toCollection(ledgerPeers)
        }
    }
}
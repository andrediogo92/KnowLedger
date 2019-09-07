package org.knowledger.agent.misc

import jade.content.AgentAction
import jade.content.onto.basic.Action
import jade.core.AID
import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.SearchConstraints
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.domain.FIPAException
import org.knowledger.agent.agents.BaseAgent
import org.tinylog.Logger


fun AgentAction.wrap(aid: AID) =
    Action(aid, this)

internal fun BaseAgent.registerLedger() {
    register {
        type = "KnowLedger"
        name = localName
        addOntologies("Transaction")
        addOntologies("Block")
        addOntologies("Ledger")
    }
}

internal fun BaseAgent.searchLedger() {
    searchService {
        type = "KnowLedger"
        addOntologies("Transaction")
        addOntologies("Block")
        addOntologies("Ledger")
    }
}

inline fun Agent.register(
    init: ServiceDescription.() -> Unit
) {
    val dfd = DFAgentDescription()
    val sd = ServiceDescription()
    dfd.name = aid

    try {
        val list = DFService.search(this, dfd)
        if (list.isNotEmpty())
            DFService.deregister(this)
        sd.init()
        dfd.addServices(sd)
        DFService.register(this, dfd)
    } catch (fe: FIPAException) {
        Logger.error(fe)
    }
}

fun Agent.searchServiceSingle(
    init: ServiceDescription.() -> Unit
): AID? {
    val dfd = DFAgentDescription()
    val sd = ServiceDescription()
    sd.init()
    dfd.addServices(sd)
    try {
        val result = DFService.search(this, dfd)
        if (result.isNotEmpty())
            return result[0].name
    } catch (fe: FIPAException) {
        Logger.error(fe)
    }

    return null
}

fun Agent.searchService(
    init: ServiceDescription.() -> Unit
): Array<AID> {
    val dfd = DFAgentDescription()
    val sd = ServiceDescription()
    sd.init()
    dfd.addServices(sd)

    val ALL = SearchConstraints()
    ALL.maxResults = -1

    try {
        val result = DFService.search(this, dfd, ALL)
        return Array(result.size) {
            result[it].name
        }

    } catch (fe: FIPAException) {
        Logger.error(fe)
    }

    return emptyArray()
}
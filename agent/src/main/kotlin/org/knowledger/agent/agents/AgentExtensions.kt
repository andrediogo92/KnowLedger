package org.knowledger.agent.agents

import jade.content.AgentAction
import jade.content.onto.basic.Action
import jade.core.AID
import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.SearchConstraints
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.domain.FIPAException
import org.tinylog.Logger

const val ledgerAgentType = "KnowLedger"
const val slaveAgentType = "KnowLedgerSlave"

fun AgentAction.wrap(aid: AID) =
    Action(aid, this)

internal operator fun <E> Set<E>?.get(rnd: Int): E? =
    this?.withIndex()?.elementAt(rnd)?.value

fun Agent.registerLedger() {
    register {
        type = ledgerAgentType
        name = localName
        addOntologies("Transaction")
        addOntologies("Block")
        addOntologies("Ledger")
    }
}

fun Agent.registerSlave() {
    register {
        type = slaveAgentType
        name = localName
        addOntologies("Transaction")
    }
}

fun Agent.searchLedger(): Array<AID> =
    searchService {
        type = ledgerAgentType
        addOntologies("Transaction")
        addOntologies("Block")
        addOntologies("Ledger")
    }

fun Agent.searchFirstLedger(): AID? =
    searchServiceSingle {
        type = ledgerAgentType
        addOntologies("Transaction")
        addOntologies("Block")
        addOntologies("Ledger")
    }

fun Agent.searchSlave(): Array<AID> =
    searchService {
        type = slaveAgentType
        addOntologies("Transaction")
    }

fun Agent.searchFirstSlave(): AID? =
    searchServiceSingle {
        type = slaveAgentType
        addOntologies("Transaction")
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

inline fun Agent.searchServiceSingle(
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

inline fun Agent.searchService(
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


private fun Agent.slaveLog(): String =
    "Slave Agent $name :>"

private fun Agent.chainLog(): String =
    "Chain Agent $name :>"

internal inline fun Agent.slaveDebug(crossinline log: () -> String) =
    Logger.debug { "${slaveLog()} ${log()}" }

internal inline fun Agent.slaveError(crossinline log: () -> String) =
    Logger.error { "${slaveLog()} ${log()}" }

internal inline fun Agent.slaveInfo(crossinline log: () -> String) =
    Logger.info { "${slaveLog()} ${log()}" }

internal inline fun Agent.chainDebug(crossinline log: () -> String) =
    Logger.debug { "${chainLog()} ${log()}" }

internal inline fun Agent.chainError(crossinline log: () -> String) =
    Logger.error { "${chainLog()} ${log()}" }

internal inline fun Agent.chainInfo(crossinline log: () -> String) =
    Logger.info { "${chainLog()} ${log()}" }
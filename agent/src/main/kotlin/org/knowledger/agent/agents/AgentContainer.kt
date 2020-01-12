package org.knowledger.agent.agents

import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.AgentController
import jade.wrapper.ContainerController
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.service.handles.LedgerHandle

/**
 * Provides thread-safe access to an agent container that
 * can run intelligent agents.
 */
class AgentContainer(
    host: String, port: Int,
    containerName: String,
    isMainContainer: Boolean,
    hasGUI: Boolean
) {
    // Create a Profile, where the launch arguments are stored
    private val profile: Profile = ProfileImpl(
        host, port,
        containerName, isMainContainer
    ).also {
        it.setParameter(Profile.GUI, hasGUI.toString())
    }

    val container: ContainerController =
        rt.createAgentContainer(profile)

    private val iAgents =
        mutableListOf<AgentController>()

    val agents: List<AgentController>
        get() = iAgents


    fun runAgent(
        name: String,
        classpath: String,
        arguments: Array<Any>
    ) {
        val agent = container.createNewAgent(
            name, classpath, arguments
        )
        synchronized(iAgents) {
            iAgents += agent
        }
        agent.start()
    }

    fun runLedgerAgent(
        name: String,
        handle: LedgerHandle,
        knownTypes: Set<AbstractStorageAdapter<out LedgerData>>,
        arguments: Array<Any>
    ) {
        runAgent(
            name, ledgerAgent,
            arrayOf(handle, knownTypes, *arguments)
        )
    }


    fun killAgent(agent: String) {
        val index =
            agents.indexOfFirst {
                it.name == agent
            }
        agents[index].kill()
        synchronized(iAgents) {
            iAgents.removeAt(index)
        }
    }

    fun runSlaveAgent(
        name: String, classpath: String,
        knownTypes: Set<AbstractStorageAdapter<out LedgerData>>,
        arguments: Array<Any>
    ) {
        runAgent(
            name, classpath,
            arrayOf(knownTypes, arguments)
        )
    }

    companion object {
        // Get the JADE runtime interface (singleton)
        private val rt: Runtime = Runtime.instance()
        const val ledgerAgent = "org.knowleder.agent.agents.ChainAgent"
    }
}
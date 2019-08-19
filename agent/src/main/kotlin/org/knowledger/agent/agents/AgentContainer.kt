package org.knowledger.agent.agents

import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.AgentController
import jade.wrapper.ContainerController
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
        arguments: Array<Any>
    ) {
        runAgent(name, ledgerAgent, arrayOf(handle, *arguments))
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

    companion object {
        // Get the JADE runtime interface (singleton)
        private val rt: Runtime = Runtime.instance()
        const val ledgerAgent = "org.knowleder.agent.agents.ChainAgent"
    }
}
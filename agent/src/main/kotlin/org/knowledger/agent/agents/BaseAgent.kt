package org.knowledger.agent.agents

import jade.content.lang.sl.SLCodec
import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAException
import org.knowledger.agent.core.ontologies.TransactionOntology
import org.tinylog.kotlin.Logger

abstract class BaseAgent : Agent() {
    private val codec = SLCodec()
    protected val agentManager: AgentManager = AgentManager(self)

    private val self: Agent
        get() = this

    override fun setup() {
        super.setup()
        contentManager.registerLanguage(codec)
        contentManager.registerOntology(TransactionOntology)
    }

    override fun takeDown() {
        super.takeDown()
        try {
            DFService.deregister(this)
        } catch (ex: FIPAException) {
            Logger.error(ex)
        }
    }
}
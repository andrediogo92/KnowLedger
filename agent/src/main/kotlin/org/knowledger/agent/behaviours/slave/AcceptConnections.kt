package org.knowledger.agent.behaviours.slave

import jade.core.Agent
import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.MessageTemplate
import org.knowledger.agent.data.PeerBook
import org.tinylog.kotlin.Logger

internal class AcceptConnections(
    agent: Agent,
    val aids: PeerBook
) : CyclicBehaviour(agent) {
    override fun action() {
        val messageTemplate =
            MessageTemplate.MatchProtocol("Slave registration")
        Logger.debug {
            "Attempt to receive connections."
        }
        agent.receive(messageTemplate)?.let {
            Logger.debug {
                "Accept connection from ${it.sender.localName}"
            }
            agent.send(it.createReply())
            //aids.registerSet(tags, it.sender)
        } ?: block()
    }

}

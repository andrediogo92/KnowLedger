package org.knowledger.agent.behaviours.ledger

import jade.content.AgentAction
import jade.content.ContentElement
import jade.content.onto.basic.Action
import jade.core.AID
import jade.core.Agent
import jade.core.behaviours.Behaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import org.knowledger.agent.agents.wrap
import org.knowledger.agent.messaging.state.ConversationIds

inline fun <T : ContentElement> Behaviour.registerReplies(
    replies: Array<T?>,
    consumer: (i: Int, t: T) -> Unit
) {
    replies.forEachIndexed { i, t ->
        if (t != null) {
            consumer(i, t)
        }
    }
}

inline fun <reified T : ContentElement> Behaviour.receiveReplies(
    agents: Array<AID>,
    conversationId: ConversationIds
): Array<T?> {
    val messageTemplate = MessageTemplate.MatchConversationId(conversationId.id)
    val replies = Array<T?>(agents.size) { null }
    for (i in 0..agents.count()) {
        val reply: ACLMessage? = agent.blockingReceive(messageTemplate, 2000)
        if (reply != null) {
            replies[agents.indexOf(reply.sender)] =
                agent.contentManager.extractContent(reply) as T
        }
    }
    return replies
}

fun <T : ContentElement> Behaviour.prepareMessage(
    agents: Array<AID>, conversationId: ConversationIds,
    performative: Int, content: T
) {
    prepareMessage(
        agents.asIterable(), conversationId, performative, content
    )
}

fun <T : ContentElement> Behaviour.prepareMessage(
    agents: Iterable<AID>, conversationId: ConversationIds,
    performative: Int, content: T
) {
    val message = ACLMessage(performative)
    message.conversationId = conversationId.id
    agent.contentManager.fillContent(
        message, content
    )
    agents.forEach {
        message.addReceiver(it)
    }
    agent.send(message)
}


fun Agent.action(a: AgentAction): Action =
    a.wrap(aid)
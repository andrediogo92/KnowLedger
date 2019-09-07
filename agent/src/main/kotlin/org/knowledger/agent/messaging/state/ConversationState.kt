package org.knowledger.agent.messaging.state

sealed class ConversationState(open val id: Int) {
    data class AwaitingReply(override val id: Int) : ConversationState(id)
    data class AwaitingSend(override val id: Int) : ConversationState(id)
}
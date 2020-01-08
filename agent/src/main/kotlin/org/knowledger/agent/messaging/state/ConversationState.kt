package org.knowledger.agent.messaging.state

enum class ConversationState {
    AwaitingReply {
        fun replied(): ConversationState = AwaitingSend
        fun prune(): ConversationState = Prune
    },
    AwaitingSend {
        fun sent(): ConversationState = AwaitingReply
        fun prune(): ConversationState = Prune
    },
    Prune
}
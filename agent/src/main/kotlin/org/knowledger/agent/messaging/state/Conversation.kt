package org.knowledger.agent.messaging.state

import jade.core.AID

data class Conversation(
    val aid: AID, val id: Int,
    val data: ConversationData,
    val state: ConversationState
) {

}
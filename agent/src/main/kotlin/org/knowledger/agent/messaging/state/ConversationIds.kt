package org.knowledger.agent.messaging.state

enum class ConversationIds(val id: String) {
    SearchLedger("searchLedger"),
    SearchSlave("searchSlave"),
    NewData("newData")
}
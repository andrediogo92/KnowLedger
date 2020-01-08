package org.knowledger.agent.agents

import java.util.concurrent.atomic.AtomicLong

internal val id: AtomicLong = AtomicLong(0)

fun generateConversationId(): Long =
    id.getAndIncrement()
package pt.um.lei.masb.agent.utils

import java.util.concurrent.atomic.AtomicLong

internal val id: AtomicLong = AtomicLong(0)

fun generateConversationId(): String =
    id.getAndIncrement().toString()
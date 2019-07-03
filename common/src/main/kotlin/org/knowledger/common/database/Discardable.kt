package org.knowledger.common.database

interface Discardable<out T : Any> {
    fun discard(): T
}
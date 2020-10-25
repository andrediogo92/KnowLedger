package org.knowledger.collections

interface ObjectPool<T : Any> {
    fun lease(): T
    fun free(element: T)
}
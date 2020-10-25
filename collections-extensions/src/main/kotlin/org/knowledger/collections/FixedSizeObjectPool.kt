package org.knowledger.collections

@Suppress("UNCHECKED_CAST")
class FixedSizeObjectPool<T : Any>(val size: Int, init: () -> T) : ObjectPool<T> {
    private val free: Array<T> = Array<Any>(size) { init() } as Array<T>
    private val leased: Array<T> = Array<Any>(size) { free[it] } as Array<T>
    private var leasedCount: Int = 0
    private var freeCount: Int = size

    @Synchronized
    override fun lease(): T {
        leased[leasedCount] = free[freeCount - 1]
        leasedCount += 1
        freeCount -= 1
        return free[freeCount]
    }

    @Synchronized
    override fun free(element: T) {
        val index = leased.asSequence().take(leasedCount).indexOf(element)
        free[freeCount] = leased[index]
        leasedCount -= 1
        leased[index] = leased[leasedCount]
        freeCount += 1
    }
}
package pt.um.lei.masb.blockchain.utils

import java.io.Serializable
import java.util.*

@Suppress("UNCHECKED_CAST")
/**
 * TODO: Needs testing.
 * An evicting queue implementation.
 * @param <E> the element type to store.
 */
class RingBuffer<E : Any>(
        val capacity: Int
) : AbstractCollection<E>(), Queue<E>, Cloneable, Serializable {

    private val buf: Array<Any?> = Array(capacity) { null } // a List implementing RandomAccess
    private var head = 0
    private var tail = 0
    override var size = 0

    override fun iterator(): MutableIterator<E> =
            RingBufferIterator()


    override fun offer(e: E): Boolean {
        try {
            if (size == capacity) {
                buf[head] = e
                head = (head + 1) % capacity
                tail = (tail + 1) % capacity
            } else {
                buf[head] = e
                head = (head + 1) % capacity
            }
        } catch (ex: Exception) {
            return false
        }
        size += 1
        return true
    }

    override fun remove(): E =
            if(size == 0) {
                throw NoSuchElementException()
            } else {
                val res = buf[head - 1]
                head = (head - 1) % capacity
                size -= 1
                res as E
            }

    override fun poll(): E? =
            if(size == 0) {
                null
            } else {
                val res = buf[head - 1]
                head = (head - 1) % capacity
                size -= 1
                res as E?
            }

    override fun element(): E =
            if (size == 0) {
                throw NoSuchElementException()
            } else {
                buf[head - 1] as E
            }

    override fun peek(): E? =
            if (size == 0) {
                null
            } else {
                buf[head - 1] as E?
            }


    private inner class RingBufferIterator<E> : MutableIterator<E> {
        /**
         * @throws NotImplementedError It's impossible to remove in place
         */
        override fun remove() {
            throw NotImplementedError("Impossible to remove in place")
        }

        private var cursor: Int = tail
        private var remaining: Int = size


        override fun hasNext(): Boolean =
                remaining > 0

        override fun next(): E {
            if (remaining > 0) {
                remaining -= 1
                val r = buf[cursor]
                cursor = (cursor + 1) % capacity
                return r as E
            } else {
                throw NoSuchElementException()
            }
        }
    }
}

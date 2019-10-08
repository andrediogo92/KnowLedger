import kotlin.math.min

@Suppress("UNCHECKED_CAST")
class RingBuffer<T : Any>(capacity: Int) {
    var elements: Array<Any> = Array(capacity) {}
    var capacity = capacity
    var writePos = 0
    var available = 0

    fun reset() {
        writePos = 0
        available = 0
    }

    fun remainingCapacity(): Int =
        capacity - available


    fun put(element: Any): Boolean =
        if (available < capacity) {
            if (writePos >= capacity) {
                writePos = 0
            }
            elements[writePos] = element
            writePos++
            available++
            true
        } else {
            false
        }

    fun put(
        newElements: Array<T>,
        length: Int = newElements.size
    ): Int {
        var readPos = 0
        return if (writePos > available) {
            //space above writePos is all empty

            if (length <= capacity - writePos) {
                //space above writePos is sufficient to insert batch

                while (readPos < length) {
                    elements[writePos] = newElements[readPos]
                    writePos++
                    readPos++
                }
                available += readPos
                length

            } else {
                //both space above writePos and below writePos is necessary to use
                //to insert batch.

                val lastEmptyPos = writePos - available

                while (writePos < capacity) {
                    elements[writePos] = newElements[readPos]
                    readPos++
                    writePos++
                }

                //fill into bottom of array too.
                writePos = 0

                val endPos = min(length - readPos, capacity - available - readPos)
                while (writePos < endPos) {
                    elements[writePos] = newElements[readPos]
                    writePos++
                    readPos++
                }
                available += readPos
                readPos
            }
        } else {
            val endPos = capacity - available + writePos

            while (writePos < endPos) {
                elements[writePos] = newElements[readPos]
                readPos++
                writePos++
            }
            available += readPos

            readPos
        }

    }


    fun take(): T? {
        if (available == 0) {
            return null
        }
        var nextSlot = writePos - available
        if (nextSlot < 0) {
            nextSlot += capacity
        }
        val nextObj = elements[nextSlot]
        available--
        return nextObj as T
    }


    fun take(into: Array<T>, length: Int = into.size): Int {
        var intoPos = 0

        return if (available <= writePos) {
            var nextPos = writePos - available
            val endPos = nextPos + min(available, length)

            while (nextPos < endPos) {
                into[intoPos] = elements[nextPos] as T
                intoPos++
                nextPos++
            }
            available -= intoPos
            intoPos
        } else {
            var nextPos = writePos - available + capacity

            val leftInTop = capacity - nextPos
            if (length <= leftInTop) {
                //copy directly
                while (intoPos < length) {
                    into[intoPos] = elements[nextPos] as T
                    nextPos++
                    intoPos++
                }
                this.available -= length
                length
            } else {
                //copy top
                while (nextPos < capacity) {
                    into[intoPos] = elements[nextPos] as T
                    intoPos++
                    nextPos++
                }

                //copy bottom - from 0 to writePos
                nextPos = 0
                val leftToCopy = length - intoPos
                val endPos = min(writePos, leftToCopy)

                while (nextPos < endPos) {
                    into[intoPos] = elements[nextPos] as T
                    intoPos++
                    nextPos++
                }

                available -= intoPos

                intoPos
            }
        }
    }
}
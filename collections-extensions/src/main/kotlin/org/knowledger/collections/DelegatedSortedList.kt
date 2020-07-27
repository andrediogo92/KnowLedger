package org.knowledger.collections

/**
 * Sorted list implementation that delegates to internal mutable list.
 * Single adding is implemented via ([binarySearch] + addAt(index)).
 * Collections are added by single adding each element.
 * Collections are removed by single removing each element.
 */
class DelegatedSortedList<E : Comparable<E>> internal constructor(
    private val delegate: MutableList<E>
) : MutableSortedList<E>,
    MutableList<E> by delegate {
    constructor() : this(mutableListOf())

    constructor(initial: Iterable<E>) : this() {
        delegate.addAll(initial)
    }

    override fun add(element: E): Boolean {
        val insertionIndex: Int = delegate.binarySearch(element)
        return if (insertionIndex < 0) {
            //Invert the inverted insertion point
            delegate.add(-insertionIndex - 1, element)
            true
        } else {
            false
        }
    }

    override fun add(index: Int, element: E) {
        val insertionIndex: Int = delegate.binarySearch(element)
        if (-insertionIndex - 1 == index) {
            //Invert the inverted insertion point
            delegate.add(index, element)
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean =
        addAll(elements)

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach(this::plusAssign)
        return true
    }

    override fun addWithIndex(element: E): Int {
        val insertionIndex: Int = delegate.binarySearch(element)
        return if (insertionIndex < 0) {
            //Invert the inverted insertion point
            delegate.add(-insertionIndex - 1, element)
            -insertionIndex - 1
        } else {
            -1
        }
    }

    override operator fun plus(list: SortedList<E>): DelegatedSortedList<E> =
        DelegatedSortedList(delegate).apply {
            addAll(list)
        }

    override fun remove(element: E): Boolean {
        val removeIndex: Int = delegate.binarySearch(element)
        return if (removeIndex >= 0) {
            delegate.removeAt(removeIndex)
            true
        } else {
            false
        }
    }

    override fun removeWithIndex(element: E): Int {
        val removeIndex = delegate.binarySearch(element)
        return if (removeIndex >= 0) {
            delegate.removeAt(removeIndex)
            removeIndex
        } else {
            -1
        }
    }

    override fun removeAll(elements: Collection<E>): Boolean =
        elements.all(this::remove)

    override operator fun minus(list: SortedList<E>): DelegatedSortedList<E> =
        apply { removeAll(list) }

    override operator fun get(index: Int): E {
        return delegate[index]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DelegatedSortedList<*>

        if (delegate != other.delegate) return false

        return true
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun replace(element: E): Boolean =
        delegate.binarySearch(element).let { index ->
            if (index >= 0) {
                delegate[index] = element
                true
            } else false
        }

    override fun replaceAt(index: Int, element: E): Boolean =
        if (delegate[index] == element) {
            delegate[index] = element
            true
        } else false
}
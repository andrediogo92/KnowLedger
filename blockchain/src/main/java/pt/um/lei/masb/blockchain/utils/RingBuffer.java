package pt.um.lei.masb.blockchain.utils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.util.*;

/**
 * TODO: Needs testing.
 * An evicting queue implementation.
 * @param <E> the element type to store.
 */
public class RingBuffer<E> extends AbstractCollection<E>
        implements Queue<E>, Cloneable, Serializable {

    private final int n; // buffer length
    private final Object buf[]; // a List implementing RandomAccess
    private int head = 0;
    private int tail = 0;
    private int size = 0;

    public RingBuffer(@Positive int capacity) {
        n = capacity;
        buf = new Object[capacity];
    }

    public @Positive int capacity() {
        return n;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return new RingBufferIterator<E>();
    }

    @Override
    public @PositiveOrZero int size() {
        return size;
    }

    @Override
    public boolean offer(E e) {
        var s = size();
        try {
            if (s == n) {
                buf[head] = e;
                head = (head + 1) % n;
                tail = (tail + 1) % n;
            } else {
                buf[head] = e;
                head = (head + 1) % n;
            }
        } catch (Exception ex) {
            return false;
        }
        size++;
        return true;
    }

    @Override
    public E remove() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        else {
            var res = (E) buf[head];
            head = (head - 1) % n;
            return res;
        }
    }

    @Override
    public E poll() {
        if(size == 0) {
            return null;
        }
        else {
            E res = (E) buf[head];
            head = (head - 1) % n;
            return res;
        }
    }

    @Override
    public E element() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else {
            return (E) buf[head];
        }
    }

    @Override
    public E peek() {
        if (size == 0) {
            return null;
        } else {
            return (E) buf[head];
        }
    }

    private class RingBufferIterator<E> implements Iterator<E> {

        private int cursor = tail;
        private int remaining = size;


        @Override
        public boolean hasNext() {
            return remaining > 0;
        }

        @Override
        public E next() {
            if (remaining > 0) {
                remaining--;
                return (E) buf[cursor++];
            }
            else {
                throw new NoSuchElementException();
            }
        }
    }
}

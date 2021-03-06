package ru.mail.polis.collections.list.todo;

import ru.mail.polis.collections.list.IDeque;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;


@SuppressWarnings("unchecked")
public class ArrayDequeSimple<E> implements IDeque<E> {


    protected E [] arrayDeque;
    protected int head;
    protected int tail;


    public ArrayDequeSimple(){
        arrayDeque = (E[])new Object[32];
    }


    private void checkInvariants() {
        assert arrayDeque[tail] == null;
        assert head == tail ? arrayDeque[head] == null :
                (arrayDeque[head] != null &&
                        arrayDeque[(tail - 1) & (arrayDeque.length - 1)] != null);
        assert arrayDeque[(head - 1) & (arrayDeque.length - 1)] == null;
    }


    protected boolean delete(int i) {
        checkInvariants();
        final Object[] elements = this.arrayDeque;
        final int mask = elements.length - 1;
        final int h = head;
        final int t = tail;
        final int front = (i - h) & mask;
        final int back  = (t - i) & mask;


        if (front >= ((t - h) & mask))
            throw new ConcurrentModificationException();


        if (front < back) {
            if (h <= i) {
                System.arraycopy(elements, h, elements, h + 1, front);
            } else {
                System.arraycopy(elements, 0, elements, 1, i);
                elements[0] = elements[mask];
                System.arraycopy(elements, h, elements, h + 1, mask - h);
            }
            elements[h] = null;
            head = (h + 1) & mask;
            return false;
        } else {
            if (i < t) {
                System.arraycopy(elements, i + 1, elements, i, back);
                tail = t - 1;
            } else {
                System.arraycopy(elements, i + 1, elements, i, mask - i);
                elements[mask] = elements[0];
                System.arraycopy(elements, 1, elements, 0, t);
                tail = (t - 1) & mask;
            }
            return true;
        }
    }


    private void doubleCapacity(){
        assert head == tail;
        int p = head;
        int n = arrayDeque.length;
        int r = n-p;
        int newCapacity = n << 1;
        if(newCapacity < 0){
            throw new IllegalStateException("Sorry, deque too big");
        }
        E[] a = (E[]) new Object[newCapacity];
        System.arraycopy(arrayDeque, p, a, 0, r); // скопировали в массив a, начиная с p(head) в 0 позицию массива назначения
        //длинной r(head - tail)
        System.arraycopy(arrayDeque, 0, a, r, p);
        arrayDeque = a;
        head = 0;
        tail = n;
    }

    @Override
    public void addFirst(E value) throws NullPointerException {
        if(value == null){
            throw new NullPointerException();
        }
        arrayDeque[head = (head - 1) & (arrayDeque.length - 1)] = value;
        if(head == tail){
            doubleCapacity();
        }
    }

    @Override
    public E removeFirst() throws NoSuchElementException{
        int h = head;
        E res = arrayDeque[h];
        if(res == null){
            throw new NoSuchElementException();
        }
        arrayDeque[h] = null;
        head = (h + 1) & (arrayDeque.length - 1);
        return res;
    }


    @Override
    public E getFirst() throws NoSuchElementException{
        if (arrayDeque[head] == null) {
            throw new NoSuchElementException();
        }
        return arrayDeque[head];
    }


    @Override
    public void addLast(E value) throws NullPointerException{
        if (value == null) {
            throw new NullPointerException();
        }
        arrayDeque[tail] = value;
        if ((tail = (tail + 1) & (arrayDeque.length - 1)) == head)
            doubleCapacity();
    }


    @Override
    public E removeLast() throws NoSuchElementException{
        if(isEmpty()){
            throw new NoSuchElementException();
        }
        int t = (tail - 1) & (arrayDeque.length - 1);
        E result = arrayDeque[t];
        if (result == null)
            return null;
        arrayDeque[t] = null;
        tail = t;
        return result;
    }


    @Override
    public E getLast() throws NoSuchElementException {
        E result = arrayDeque[(tail - 1) & (arrayDeque.length - 1)];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }


    @Override
    public boolean contains(E value) throws NullPointerException {
        if(value == null){
            throw new NullPointerException();
        }
        int mask = arrayDeque.length - 1;
        int i = head;
        Object x;
        while ( (x = arrayDeque[i]) != null) {
            if (value.equals(x))
                return true;
            i = (i + 1) & mask;
        }
        return false;
    }


    @Override
    public int size() {
        return (tail - head) & (arrayDeque.length - 1);
    }

    @Override
    public boolean isEmpty() {
        return ((tail - head) & (arrayDeque.length - 1)) == 0;
    }


    @Override
    public void clear() {
        int h = head;
        int t = tail;
        if (h != t) {
            head = tail = 0;
            int i = h;
            int mask = arrayDeque.length - 1;
            do {
                arrayDeque[i] = null;
                i = (i + 1) & mask;
            } while (i != t);
        }
    }


    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int index = head;
            private int cursor = -1;
            private int tailIterator = tail;

            @Override
            public boolean hasNext() {
                return index != tailIterator;
            }

            @Override
            public E next() throws ConcurrentModificationException, NoSuchElementException {
                if (index == tailIterator) {
                    throw new NoSuchElementException();
                }
                E result = arrayDeque[index];
                if (tailIterator != tail || result == null) {
                    throw new ConcurrentModificationException();
                }
                cursor = index;
                index = (index + 1) & (arrayDeque.length - 1);
                return result;
            }
            @Override
            public void remove(){
                if (cursor < 0)
                    throw new IllegalStateException();
                if (delete(cursor)) {
                    cursor = (cursor - 1) & (arrayDeque.length - 1);
                    tailIterator = tail;
                }
                cursor = -1;
            }
        };
    };
}
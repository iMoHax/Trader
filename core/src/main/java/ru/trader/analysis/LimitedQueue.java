package ru.trader.analysis;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LimitedQueue<E> extends ArrayList<E> implements Queue<E> {
    private final Comparator<? super E> comparator;
    private final int limit;
    private boolean sorted=false;

    public LimitedQueue(int initialCapacity, int limit) {
        this(initialCapacity, limit, null);
    }

    public LimitedQueue(int initialCapacity, int limit, Comparator<? super E> comparator) {
        super(initialCapacity);
        this.limit = limit;
        this.comparator = comparator;
    }

    public LimitedQueue(int limit) {
        this(limit, null);
    }

    public LimitedQueue(int limit, Comparator<? super E> comparator) {
        this.limit = limit;
        this.comparator = comparator;
    }

    public LimitedQueue(Collection<? extends E> c, int limit) {
        this(c, limit, null);
    }

    public LimitedQueue(Collection<? extends E> c, int limit, Comparator<? super E> comparator) {
        super(c);
        this.limit = limit;
        this.comparator = comparator;
        trimToLimit();
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove() {
        return remove(0);
    }

    @Override
    public E poll() {
        if (isEmpty()) return null;
        return remove(0);
    }

    @Override
    public E element() {
        return get(0);
    }

    @Override
    public E peek() {
        if (isEmpty()) return null;
        return get(0);
    }

    public E last() {
        if (isEmpty()) return null;
        return get(size()-1);
    }

    @Override
    public E get(int index) {
        sort();
        return super.get(index);
    }

    @Override
    public E remove(int index) {
        sort();
        return super.remove(index);
    }

    @Override
    public boolean add(E element) {
        if (comparator != null){
            return addToTop(element);
        } else {
            return size() < limit && super.add(element);
        }
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        if (comparator != null){
            return addAllToTop(c);
        } else {
            E[] a = (E[]) c.toArray();
            int numNew = Math.min(a.length, limit - size());
            return numNew == 0 || super.addAll(Arrays.asList(a).subList(0, numNew));
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    public void sort(){
        if (sorted || comparator == null) return;
        super.sort(comparator);
        sorted = true;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException();
    }


    private boolean addToTop(E element){
        int size = super.size();
        if (sorted || size == limit) {
            sort();
            int index = indexedBinarySearch(element, comparator);
            if (index < 0) index = -1 - index;
            if (index == limit || index < size && element.equals(super.get(index))) return false;
            super.add(index, element);
            if (size == limit)
                super.remove(limit);
        } else {
            super.add(element);
        }
        return true;

    }

    private boolean addAllToTop(Collection<? extends E> c){
        if (c.isEmpty()) return true;
        int size = super.size();
        int len = c.size();
        if (sorted || size + len >= limit) {
            List<E> a = toList(c);
            if (size == 0){
                super.addAll(0, a.subList(0, Math.min(limit, len)));
                sorted = true;
            } else {
                sort();
                E first = super.get(0);
                E last = super.get(size - 1);
                E aFirst = a.get(0);
                E aLast = a.get(len-1);
                if (size == limit && comparator.compare(aFirst, last) >= 0) return true;
                if (comparator.compare(first, aLast) >= 0){
                    super.addAll(0, a.subList(0, Math.min(limit, len)));
                    trimToLimit();
                    return true;
                }
                for (int i = 0; i < len; i++) {
                    E element = a.get(i);
                    int index = indexedBinarySearch(element, comparator);
                    if (index < 0) index = -1 - index;
                    else {
                        if (element.equals(super.get(index))) continue;
                    }
                    if (index >= limit) break;
                    super.add(index, element);
                }
                if (super.size() > limit)
                    super.remove(limit);
            }
        } else {
            super.addAll(0, c);
        }
        return true;

    }

    private void trimToLimit(){
        int size = super.size();
        if (limit >= size) return;
        super.removeRange(limit, size);
    }

    @SuppressWarnings("unchecked")
    private List<E> toList(Collection<? extends E> c){
        if (c instanceof LimitedQueue && comparator == ((LimitedQueue) c).comparator) {
            ((LimitedQueue) c).sort();
            return (List<E>) c;
        } else {
            E[] a = (E[]) c.toArray();
            Arrays.sort(a, comparator);
            return Arrays.asList(a);
        }
    }

    private int indexedBinarySearch(E key, Comparator<? super E> c) {
        int low = 0;
        int high = size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            E midVal = super.get(mid);
            int cmp = c.compare(midVal, key);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

}

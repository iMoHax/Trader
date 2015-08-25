package ru.trader.analysis;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class GroupLimitedQueue<E> implements Queue<E> {
    private final Comparator<? super E> comparator;
    private final Function<E, Object> groupGetter;
    private final ArrayList<QueueWrapper> queues;
    private final int limit;
    private boolean sorted = false;

    public GroupLimitedQueue(int limit, Function<E, Object> groupGetter) {
        this(limit, null, groupGetter);
    }

    public GroupLimitedQueue(int limit, Comparator<? super E> comparator, Function<E, Object> groupGetter) {
        this.limit = limit;
        this.comparator = comparator;
        this.groupGetter = groupGetter;
        queues = new ArrayList<>(10);
    }

    private LimitedQueue<E> getGroup(Object group, boolean add){
        for (QueueWrapper q : queues) {
            if (q.group.equals(group)){
                return q.queue;
            }
        }
        if (add) {
            LimitedQueue<E> queue = new LimitedQueue<>(limit, comparator);
            queues.add(new QueueWrapper(group, queue));
            sorted = false;
            return queue;
        } else {
            return null;
        }
    }

    private LimitedQueue<E> getGroup(int groupIndex){
        return queues.get(groupIndex).queue;
    }

    private boolean add(Object group, E element){
        LimitedQueue<E> queue = getGroup(group, true);
        boolean res = queue.add(element);
        if (res) sorted = false;
        return res;
    }

    private boolean remove(Object group, E element){
        LimitedQueue<E> queue = getGroup(group, false);
        if (queue == null) return false;
        boolean res = queue.remove(element);
        if (res) sorted = false;
        return res;
    }

    private E remove(int groupIndex, int elementIndex){
        LimitedQueue<E> queue = getGroup(groupIndex);
        sorted = false;
        return queue.remove(elementIndex);
    }

    private E get(int groupIndex, int elementIndex){
        LimitedQueue<E> queue = getGroup(groupIndex);
        return queue.get(elementIndex);
    }

    @Override
    public boolean add(E element) {
        return add(groupGetter.apply(element), element);
    }

    @Override
    public boolean offer(E element) {
        return add(element);
    }

    @Override
    public E remove() {
        sort();
        return remove(0, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object element) {
        if (element == null){
            boolean res = false;
            for (QueueWrapper q : queues) {
                res = q.queue.remove(element) || res;
            }
            return res;
        } else {
            E e = (E) element;
            LimitedQueue<E> queue = getGroup(groupGetter.apply(e), false);
            return queue != null && remove(queue, e);
        }
    }

    @Override
    public E poll() {
        if (isEmpty()) return null;
        sort();
        return remove(0, 0);
    }

    @Override
    public E element() {
        sort();
        return get(0, 0);
    }

    @Override
    public E peek() {
        if (isEmpty()) return null;
        sort();
        return get(0, 0);
    }

    @Override
    public int size() {
        int size = 0;
        for (QueueWrapper q : queues) {
            size += q.queue.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        if (o == null){
            for (QueueWrapper q : queues) {
                if (q.queue.contains(o)){
                    return true;
                }
            }
        } else {
            LimitedQueue<E> queue = getGroup(groupGetter.apply((E) o), false);
            return queue != null && queue.contains(o);
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<QueueWrapper> qIter = queues.iterator();
            private Iterator<E> iterator;
            private E next;
            {
                nextQueue();
            }

            private void nextQueue(){
                if (qIter.hasNext()) {
                    iterator = qIter.next().queue.iterator();
                    nextEntry();
                } else {
                    next = null;
                }
            }

            private void nextEntry(){
                if (iterator.hasNext()){
                    next = iterator.next();
                } else {
                    next = null;
                }
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public E next() {
                E res  = next;
                if (iterator.hasNext()){
                    nextEntry();
                } else {
                    nextQueue();
                }
                return res;
            }
        };
    }

    @NotNull
    @Override
    public Object[] toArray() {
        Object[] r = new Object[size()];
        int index = 0;
        for (QueueWrapper q : queues) {
            Object[] a = q.queue.toArray();
            int s = a.length;
            System.arraycopy(a, 0, r, index, s);
            index += s;
        }
        return r;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        int size = size();
        T[] r = a.length >= size ? a : (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        int index = 0;
        for (QueueWrapper q : queues) {
            T[] qa = (T[]) q.queue.toArray();
            int s = qa.length;
            System.arraycopy(qa, 0, r, index, s);
            index += s;
        }
        return r;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)){
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean modified = false;
        if (c instanceof GroupLimitedQueue){
            GroupLimitedQueue<E> groupQueue = (GroupLimitedQueue<E>) c;
            for (QueueWrapper q : groupQueue.queues) {
                LimitedQueue<E> queue = getGroup(q.group, true);
                modified = queue.addAll(q.queue) || modified;
            }
            sorted = sorted && !modified;
        } else {
            for (E element : c) {
                modified = modified || add(element);
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean modified = false;
        for (QueueWrapper q : queues) {
            modified = q.queue.removeAll(c) || modified;
        }
        sorted = sorted && !modified;
        return modified;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean modified = false;
        for (QueueWrapper q : queues) {
            modified = q.queue.retainAll(c) || modified;
        }
        sorted = sorted && !modified;
        return modified;
    }

    @Override
    public void clear() {
        sorted = false;
        queues.clear();
    }

    public void sort(){
        if (sorted || comparator == null) return;
        for (QueueWrapper q : queues) {
            q.queue.sort();
        }
        queues.sort((q1, q2) -> {
            E e1 = q1.queue.peek();
            E e2 = q2.queue.peek();
            if (e1 == null || e2 == null){
                return e1 == e2 ? 0 : e1 == null ? 1 : -1;
            }
            return comparator.compare(e1, e2);
        });
        sorted = true;
    }

    private class QueueWrapper {
        private final Object group;
        private final LimitedQueue<E> queue;

        private QueueWrapper(Object group, LimitedQueue<E> queue) {
            this.group = group;
            this.queue = queue;
        }
    }
}

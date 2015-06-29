package ru.trader.analysis;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;

public class LimitedQueueTest  extends Assert {

    @Test
    public void testAdd() throws Exception {
        LimitedQueue<Integer> queue = new LimitedQueue<>(5,3);
        queue.add(3);
        assertEquals(1, queue.size());
        assertEquals(3, queue.get(0).intValue());
        queue.add(2);
        assertEquals(2, queue.size());
        assertEquals(3, queue.get(0).intValue());
        queue.add(1);
        assertEquals(3, queue.size());
        assertEquals(3, queue.get(0).intValue());
        assertEquals(1, queue.get(2).intValue());
        queue.add(4);
        assertEquals(3, queue.size());
        assertEquals(3, queue.get(0).intValue());
        assertEquals(1, queue.get(2).intValue());

        queue = new LimitedQueue<>(3, Comparator.<Integer>naturalOrder());
        queue.add(4);
        assertEquals(1, queue.size());
        assertEquals(4, queue.get(0).intValue());
        queue.add(2);
        assertEquals(2, queue.size());
        assertEquals(2, queue.get(0).intValue());
        queue.add(1);
        assertEquals(3, queue.size());
        assertEquals(1, queue.get(0).intValue());
        assertEquals(4, queue.get(2).intValue());
        queue.add(3);
        assertEquals(3, queue.size());
        assertEquals(1, queue.get(0).intValue());
        assertEquals(3, queue.get(2).intValue());

        queue = new LimitedQueue<>(5, Comparator.<Integer>reverseOrder());
        queue.add(2);
        queue.add(4);
        queue.add(1);
        queue.add(3);
        assertEquals(4, queue.get(0).intValue());
        assertEquals(4, queue.size());
        assertEquals(1, queue.get(3).intValue());
    }

    @Test
    public void testPoolPeek() throws Exception {
        LimitedQueue<Integer> queue = new LimitedQueue<>(3);
        queue.add(3);
        queue.add(2);
        queue.add(1);
        assertEquals(3, queue.size());
        Integer a = queue.peek();
        assertEquals(3, a.intValue());
        assertEquals(3, queue.size());
        a = queue.peek();
        assertEquals(3, a.intValue());
        assertEquals(3, queue.size());
        a = queue.peek();
        assertEquals(3, a.intValue());
        assertEquals(3, queue.size());

        a = queue.poll();
        assertEquals(3, a.intValue());
        assertEquals(2, queue.size());
        a = queue.poll();
        assertEquals(2, a.intValue());
        assertEquals(1, queue.size());
        a = queue.poll();
        assertEquals(1, a.intValue());
        assertEquals(0, queue.size());
        a = queue.poll();
        assertNull(a);

        queue = new LimitedQueue<>(3, Comparator.<Integer>naturalOrder());
        queue.add(4);
        queue.add(2);
        queue.add(1);
        queue.add(3);

        assertEquals(3, queue.size());
        a = queue.peek();
        assertEquals(1, a.intValue());
        assertEquals(3, queue.size());
        a = queue.peek();
        assertEquals(1, a.intValue());
        assertEquals(3, queue.size());
        a = queue.peek();
        assertEquals(1, a.intValue());
        assertEquals(3, queue.size());

        a = queue.poll();
        assertEquals(1, a.intValue());
        assertEquals(2, queue.size());
        a = queue.poll();
        assertEquals(2, a.intValue());
        assertEquals(1, queue.size());
        a = queue.poll();
        assertEquals(3, a.intValue());
        assertEquals(0, queue.size());
        a = queue.poll();
        assertNull(a);
    }


    @Test
    public void testAddAll() throws Exception {
        LimitedQueue<Integer> queue = new LimitedQueue<>(Arrays.asList(5,3,4), 5, Comparator.naturalOrder());
        assertEquals(3, queue.size());
        assertEquals(3, queue.peek().intValue());

        queue.addAll(Arrays.asList(0,1,2));
        assertEquals(5, queue.size());
        assertEquals(0, queue.peek().intValue());
        assertEquals(4, queue.last().intValue());

        queue = new LimitedQueue<>(Arrays.asList(5,3,4), 6, Comparator.naturalOrder());
        queue.addAll(Arrays.asList(1,2));
        assertEquals(5, queue.size());
        assertEquals(1, queue.peek().intValue());
        assertEquals(5, queue.last().intValue());

        queue = new LimitedQueue<>(Arrays.asList(5,3,4), 3, Comparator.naturalOrder());
        queue.addAll(Arrays.asList(6,10,7));
        assertEquals(3, queue.size());
        assertEquals(3, queue.peek().intValue());
        assertEquals(5, queue.last().intValue());

        queue = new LimitedQueue<>(Arrays.asList(5,3,4,9), 3, Comparator.naturalOrder());
        queue.addAll(Arrays.asList(6,1,10));
        assertEquals(3, queue.size());
        assertEquals(1, queue.peek().intValue());
        assertEquals(4, queue.last().intValue());

        queue = new LimitedQueue<>(4, Comparator.naturalOrder());
        queue.addAll(Arrays.asList(6,1,10));
        assertEquals(3, queue.size());
        assertEquals(1, queue.peek().intValue());
        assertEquals(10, queue.last().intValue());

        queue = new LimitedQueue<>(Arrays.asList(2,1,4,9), 4, Comparator.naturalOrder());
        queue.addAll(Arrays.asList(6,3,7));
        assertEquals(4, queue.size());
        assertEquals(1, queue.peek().intValue());
        assertEquals(4, queue.last().intValue());

        queue = new LimitedQueue<>(Arrays.asList(2,6,4,9,5), 10, Comparator.naturalOrder());
        queue.addAll(Arrays.asList(1,6,3,7,11,5,10,15));
        assertEquals(10, queue.size());
        assertEquals(1, queue.peek().intValue());
        assertEquals(11, queue.last().intValue());


        queue = new LimitedQueue<>(Arrays.asList(2,6,4,9,5), 10, Comparator.naturalOrder());
        LimitedQueue<Integer> queue2 = new LimitedQueue<>(Arrays.asList(1,6,3,7,11,5,10,15), 10, Comparator.naturalOrder());
        queue.addAll(queue2);
        assertEquals(10, queue.size());
        assertEquals(1, queue.peek().intValue());
        assertEquals(11, queue.last().intValue());

        queue = new LimitedQueue<>(Arrays.asList(2,6,4,9,5), 10, Comparator.naturalOrder());
        queue2 = new LimitedQueue<>(Arrays.asList(1,6,3,7,11,5,10,15), 10, Comparator.reverseOrder());
        queue.addAll(queue2);
        assertEquals(10, queue.size());
        assertEquals(1, queue.peek().intValue());
        assertEquals(11, queue.last().intValue());

    }

    @Test
    public void testAddAll2() throws Exception {
        LimitedQueue<Integer> queue = new LimitedQueue<>(Arrays.asList(5,3,4), 5);
        assertEquals(3, queue.size());
        assertEquals(5, queue.peek().intValue());

        queue.addAll(Arrays.asList(0,1,2));
        assertEquals(5, queue.size());
        assertEquals(5, queue.peek().intValue());
        assertEquals(1, queue.last().intValue());

        queue = new LimitedQueue<>(Arrays.asList(5,3,4), 6);
        queue.addAll(Arrays.asList(1,2));
        assertEquals(5, queue.size());
        assertEquals(5, queue.peek().intValue());
        assertEquals(2, queue.last().intValue());
    }


}
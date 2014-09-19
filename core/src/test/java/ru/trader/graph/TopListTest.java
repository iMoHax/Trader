package ru.trader.graph;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class TopListTest extends Assert {

    private static final Function<Integer, Integer> getGroup = (o1) -> Math.floorDiv(o1, 10);

    private static final Comparator<Integer> groupcomp = (o1, o2) -> {
        int cmp = Integer.compare(getGroup.apply(o1), getGroup.apply(o2));
        if (cmp !=0 ) return cmp;
        return o1.compareTo(o2);
    };

    private void add(List<Integer> list, Integer entry){
        TopList.addToGroupTop(list, entry, 10, groupcomp, getGroup, 1);
    }

    @Test
    public void testAddToGroup() throws Exception {
        ArrayList<Integer> top = new ArrayList<>(10);
        add(top, 5);
        add(top, 15);
        add(top, 22);
        add(top, 34);
        add(top, 36);
        add(top, 21);
        add(top, 7);
        add(top, 6);
        add(top, 3);

        assertEquals(4, top.size());
        assertEquals(3, top.get(0).intValue());
        assertEquals(15, top.get(1).intValue());
        assertEquals(21, top.get(2).intValue());
        assertEquals(34, top.get(3).intValue());
    }

    @Test
    public void testAddToGroup2() throws Exception {
        ArrayList<Integer> top = new ArrayList<>(10);
        add(top, 36);
        add(top, 15);
        add(top, 22);
        add(top, 6);
        add(top, 34);
        add(top, 5);
        add(top, 21);
        add(top, 7);
        add(top, 3);

        assertEquals(4, top.size());
        assertEquals(3, top.get(0).intValue());
        assertEquals(15, top.get(1).intValue());
        assertEquals(21, top.get(2).intValue());
        assertEquals(34, top.get(3).intValue());
    }


    @Test
    public void testAddToGroup3() throws Exception {
        ArrayList<Integer> top = new ArrayList<>(10);
        add(top, 21);
        add(top, 34);
        add(top, 36);
        add(top, 3);
        add(top, 15);
        add(top, 22);
        add(top, 6);
        add(top, 34);
        add(top, 5);
        add(top, 7);

        assertEquals(4, top.size());
        assertEquals(3, top.get(0).intValue());
        assertEquals(15, top.get(1).intValue());
        assertEquals(21, top.get(2).intValue());
        assertEquals(34, top.get(3).intValue());
    }

}

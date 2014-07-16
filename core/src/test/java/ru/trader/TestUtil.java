package ru.trader;


import org.junit.Assert;

import java.util.Collection;
import java.util.Iterator;

public class TestUtil {

    @SafeVarargs
    public static <T> void assertCollectionEquals(Collection<T> collection, T... items){
        assertSize(collection, items);
        int curIndx=0;
        for (T actual : collection) {
            if (!actual.equals(items[curIndx])){
                Assert.fail(String.format("Entry by index %d is different. Expected: %s Actual: %s", curIndx, items[curIndx], actual));
                return;
            }
            curIndx++;
        }

    }

    private static <T> void assertSize(Collection<T> collection, T[] items){
        int expectedSize = items.length;
        int actualSize = collection.size();
        if (actualSize!=expectedSize) {
            Assert.fail(String.format("Collection size differed. Expected: %d Actual: %d", expectedSize, actualSize));
        }
    }

    private static <T> void assertSize(Iterable<T> collection, T[] items){
        int expectedSize = items.length;
        Iterator<T> iterator = collection.iterator();
        int actualSize=0;
        while (iterator.hasNext()){actualSize++;iterator.next();}
        if (actualSize!=expectedSize) {
            Assert.fail(String.format("Collection size differed. Expected: %d Actual: %d", expectedSize, actualSize));
        }
    }

    private static <T> void checkContains(Collection<T> collection, boolean all, T[] items){
        if (all) assertSize(collection, items);
        for (T item : items) {
            if (!collection.contains(item)){
                Assert.fail(String.format("Collection should include an item %s", item));
                return;
            }
        }
    }


    private static <T> boolean contains(Iterable<T> collection, T item){
        for (T t : collection) {
            if (item.equals(t)) return true;
        }
        return false;
    }

    private static <T> void checkContains(Iterable<T> collection, boolean all, T[] items){
        if (all) assertSize(collection, items);
        for (T item : items) {
            if (!contains(collection, item)){
                Assert.fail(String.format("Collection should include an item %s", item));
                return;
            }
        }
    }

    @SafeVarargs
    public static <T> void assertCollectionNoContain(Collection<T> collection, T... items){
        for (T item : items) {
            if (collection.contains(item)){
                Assert.fail(String.format("Collection must not contain item %s", item));
                return;
            }
        }
    }


    @SafeVarargs
    public static <T> void assertCollectionContain(Collection<T> collection, T... items){
        checkContains(collection, false, items);
    }

    @SafeVarargs
    public static <T> void assertCollectionContainAll(Collection<T> collection,  T... items){
        checkContains(collection, true, items);
    }

    @SafeVarargs
    public static <T> void assertIterableContain(Iterable<T> collection, T... items){
        checkContains(collection, false, items);
    }

    @SafeVarargs
    public static <T> void assertIterableContainAll(Iterable<T> collection,  T... items){
        checkContains(collection, true, items);
    }

}

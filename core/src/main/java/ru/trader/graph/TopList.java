package ru.trader.graph;

import java.util.*;
import java.util.function.Function;

public class TopList<T> {
    protected final List<T> list;
    protected final int limit;
    protected final Comparator<T> comparator;

    public TopList(int limit) {
        this(limit, null);
    }


    public TopList(int limit, Comparator<T> comparator) {
        this.list = new ArrayList<>(limit);
        this.limit = limit;
        this.comparator = comparator;
    }

    //return true if is last entry or list is full
    public boolean add(T entry){
        if (comparator != null){
            addToTop(list, entry, limit, comparator);
        } else {
            if (list.size() >= limit) return false;
            list.add(entry);
            if (list.size() >= limit) return false;
        }
        return true;
    }

    public List<T> getList() {
        return list;
    }

    public static <T> void addToGroupTop(List<T> list, T entry, int limit, Comparator<T> comparator, Function<T, Integer> getGroup, int groupSize) {
        boolean isFull = list.size() >= limit;
        int group = getGroup.apply(entry);
        int groupStart = groupSize * group;
        int groupEnd = groupSize * (group + 1);
        if (!isFull){
            if (groupStart >= list.size()) groupStart = list.size();
            if (groupEnd >= list.size()) groupEnd = list.size();
        }
        List<T> groupList = list.subList(groupStart, groupEnd);
        T removeEntry = addToTop(groupList, entry, groupSize, comparator);
        if (!isFull && removeEntry != null && group != getGroup.apply(removeEntry)){
            addToGroupTop(list, removeEntry, limit, comparator, getGroup, groupSize);
        }
    }

    public static <T> T addToTop(List<T> list, T entry, int limit, Comparator<T> comparator) {
        if (list.size() == limit) {
            int index = Collections.binarySearch(list, entry, comparator);
            if (index < 0) index = -1 - index;
            if (index == limit) return null;
            list.add(index, entry);
            return list.remove(limit);

        } else {
            if (list.size() < limit - 1) {
                list.add(entry);
            } else {
                list.add(entry);
                list.sort(comparator);
            }
        }
        return null;
    }

    public static <T> void addAllToTop(List<T> list, Collection<T> sortEntries, int limit, Comparator<T> comparator) {
        for (T entry : sortEntries) {
            if (list.size() == limit) {
                int index = Collections.binarySearch(list, entry, comparator);
                if (index < 0) index = -1 - index;
                if (index == limit) return;
                list.add(index, entry);
                list.remove(limit);
            } else {
                if (list.size() < limit - 1) {
                    list.add(entry);
                } else {
                    list.add(entry);
                    list.sort(comparator);
                }
            }
        }
    }
}
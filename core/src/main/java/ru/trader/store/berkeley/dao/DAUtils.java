package ru.trader.store.berkeley.dao;


import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.SecondaryIndex;

public class DAUtils {

    public static <T, K, E> T get(EntityIndex<K, E> index, K key, Function<E, T> convertFunc){
        E entry = index.get(key);
        return entry != null ? convertFunc.apply(entry) : null;
    }

    public static <T, K, E> Collection<T> get(EntityIndex<K, E> index, K from, K to, Function<E, T> convertFunc){
        return get(index, from, to, convertFunc, null);
    }

    public static <T, K, E> Collection<T> get(EntityIndex<K, E> index, K from, K to, Function<E, T> convertFunc, Predicate<E> filter){
        Collection<T> res = new LinkedList<>();
        try (EntityCursor<E> cursor = index.entities(from, true, to, true)){
            for(E entity : cursor){
                if (filter == null || filter.test(entity)){
                    res.add(convertFunc.apply(entity));
                }
            }
        }
        return res;
    }
    public static <T, E> Collection<T> getAll(EntityIndex<?, E> index, Function<E, T> convertFunc){
        return getAll(index, convertFunc, null);
    }

    public static <T, E> Collection<T> getAll(EntityIndex<?, E> index, Function<E, T> convertFunc, Predicate<E> filter){
        Collection<T> res = new LinkedList<>();
        try (EntityCursor<E> cursor = index.entities()){
            for(E entity : cursor){
                if (filter == null || filter.test(entity)){
                    res.add(convertFunc.apply(entity));
                }
            }
        }
        return res;
    }

    public static <T, K, E> Collection<T> getAll(SecondaryIndex<K, ?, E> index, K key, Function<E, T> convertFunc){
        return getAll(index, key, convertFunc, null);
    }

    public static <T, K, E> Collection<T> getAll(SecondaryIndex<K, ?, E> index, K key, Function<E, T> convertFunc, Predicate<E> filter){
        Collection<T> res = new LinkedList<>();
        try (EntityCursor<E> cursor = index.subIndex(key).entities()){
            for(E entity : cursor){
                if (filter == null || filter.test(entity)){
                    res.add(convertFunc.apply(entity));
                }
            }
        }
        return res;
    }

}

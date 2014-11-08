package ru.trader.store.berkeley.dao;


import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.SecondaryIndex;

public class DAUtils {

    public static <T, K, E> T get(EntityIndex<K, E> index, K key, Function<E, T> convertFunc){
        return convertFunc.apply(index.get(key));
    }

    public static <T, K, E> Collection<T> get(EntityIndex<K, E> index, K from, K to, Function<E, T> convertFunc){
        Collection<T> res = new LinkedList<>();
        try (EntityCursor<E> cursor = index.entities(from, true, to, true)){
            for(E entity : cursor){
                res.add(convertFunc.apply(entity));
            }
        }
        return res;
    }

    public static <T, E> Collection<T> getAll(EntityIndex<?, E> index, Function<E, T> convertFunc){
        Collection<T> res = new LinkedList<>();
        try (EntityCursor<E> cursor = index.entities()){
            for(E entity : cursor){
                res.add(convertFunc.apply(entity));
            }
        }
        return res;
    }

    public static <T, K, E> Collection<T> getAll(SecondaryIndex<K, ?, E> index, K key, Function<E, T> convertFunc){
        Collection<T> res = new LinkedList<>();
        try (EntityCursor<E> cursor = index.subIndex(key).entities()){
            for(E entity : cursor){
                res.add(convertFunc.apply(entity));
            }
        }
        return res;
    }

}

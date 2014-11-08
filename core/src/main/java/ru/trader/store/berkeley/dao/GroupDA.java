package ru.trader.store.berkeley.dao;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import ru.trader.store.berkeley.entities.BDBGroup;

import java.util.Collection;
import java.util.function.Function;

public class GroupDA<T> {
    private final PrimaryIndex<String, BDBGroup> indexById;
    private final Function<BDBGroup,T> convertFunc;

    public GroupDA(EntityStore store, Function<BDBGroup, T> convertFunc) {
        this.convertFunc = convertFunc;
        this.indexById = store.getPrimaryIndex(String.class, BDBGroup.class);
    }

    public T get(String name){
        return DAUtils.get(indexById, name, convertFunc);
    }

    public Collection<T> getAll(){
        return DAUtils.getAll(indexById, convertFunc);
    }

    public BDBGroup put(BDBGroup group){
        return indexById.put(group);
    }

    public void update(BDBGroup group){
        indexById.putNoReturn(group);
    }

    public void delete(BDBGroup group){
        indexById.delete(group.getName());
    }
}

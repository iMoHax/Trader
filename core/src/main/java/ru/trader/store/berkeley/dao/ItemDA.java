package ru.trader.store.berkeley.dao;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import ru.trader.store.berkeley.entities.BDBItem;

import java.util.Collection;
import java.util.function.Function;

public class ItemDA<T> {
    private final PrimaryIndex<Long, BDBItem> indexById;
    private final SecondaryIndex<String, Long, BDBItem> indexByGroupId;
    private final Function<BDBItem,T> convertFunc;

    public ItemDA(EntityStore store, Function<BDBItem, T> convertFunc) {
        this.convertFunc = convertFunc;
        this.indexById = store.getPrimaryIndex(Long.class, BDBItem.class);
        this.indexByGroupId = store.getSecondaryIndex(indexById, String.class, "groupId");
    }

    public T get(long id){
        return DAUtils.get(indexById, id, convertFunc);
    }

    public T get(String name){
        Collection<T> items = DAUtils.getAll(indexById, convertFunc, item -> item.getName().equals(name));
        return items.isEmpty() ? null : items.iterator().next();
    }

    public Collection<T> getAll(){
        return DAUtils.getAll(indexById, convertFunc);
    }

    public Collection<T> getAll(String group){
        return DAUtils.getAll(indexByGroupId, group, convertFunc);
    }

    public BDBItem put(BDBItem item){
        return indexById.put(item);
    }

    public void update(BDBItem item){
        indexById.putNoReturn(item);
    }

    public void delete(BDBItem item){
        indexById.delete(item.getId());
    }
}

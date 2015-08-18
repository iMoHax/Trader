package ru.trader.store.berkeley.dao;

import com.sleepycat.persist.*;
import ru.trader.store.berkeley.entities.BDBVendor;

import java.util.Collection;
import java.util.function.Function;

public class VendorDA<T> {
    private final PrimaryIndex<Long, BDBVendor> indexById;
    private final SecondaryIndex<Long, Long, BDBVendor> indexByPlaceId;
    private final Function<BDBVendor,T> convertFunc;

    public VendorDA(EntityStore store, Function<BDBVendor, T> convertFunc) {
        this.convertFunc = convertFunc;
        this.indexById = store.getPrimaryIndex(Long.class, BDBVendor.class);
        this.indexByPlaceId = store.getSecondaryIndex(indexById, Long.class, "placeId");
    }

    public T get(long id){
        return DAUtils.get(indexById, id, convertFunc);
    }

    public Collection<T> getAll(){
        return DAUtils.getAll(indexById, convertFunc);
    }

    public Collection<T> getAllByPlace(long placeId){
        return DAUtils.getAll(indexByPlaceId, placeId, convertFunc);
    }

    public boolean contains(long placeId){
        return indexByPlaceId.contains(placeId);
    }

    public long count(long placeId){
        return indexByPlaceId.subIndex(placeId).count();
    }

    public BDBVendor put(BDBVendor vendor){
        return indexById.put(vendor);
    }

    public void update(BDBVendor vendor){
        indexById.putNoReturn(vendor);
    }

    public void delete(BDBVendor vendor){
        indexById.delete(vendor.getId());
    }
}

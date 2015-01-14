package ru.trader.store.berkeley.dao;

import com.sleepycat.persist.*;
import ru.trader.core.OFFER_TYPE;
import ru.trader.store.berkeley.entities.BDBOffer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.function.Function;

public class OfferDA<T> {
    private final PrimaryIndex<Long, BDBOffer> indexById;
    private final SecondaryIndex<Long, Long, BDBOffer> indexByItemId;
    private final SecondaryIndex<Long, Long, BDBOffer> indexByVendorId;
    private final SecondaryIndex<OFFER_TYPE, Long, BDBOffer> indexByType;
    private final Function<BDBOffer,T> convertFunc;

    public OfferDA(EntityStore store, Function<BDBOffer, T> convertFunc) {
        this.convertFunc = convertFunc;
        this.indexById = store.getPrimaryIndex(Long.class, BDBOffer.class);
        this.indexByItemId = store.getSecondaryIndex(indexById, Long.class, "itemId");
        this.indexByVendorId = store.getSecondaryIndex(indexById, Long.class, "vendorId");
        this.indexByType = store.getSecondaryIndex(indexById, OFFER_TYPE.class, "type");
    }

    public T get(long id){
        return DAUtils.get(indexById, id, convertFunc);
    }

    public Collection<T> getAll(){
        return DAUtils.getAll(indexById, convertFunc);
    }

    public Collection<T> getAllByItem(long itemId){
        return DAUtils.getAll(indexByItemId, itemId, convertFunc);
    }

    public Collection<T> getAllByVendor(long vendorId){
        return DAUtils.getAll(indexByVendorId, vendorId, convertFunc);
    }

    public Collection<T> getAllByType(OFFER_TYPE type){
        return DAUtils.getAll(indexByType, type, convertFunc);
    }

    public T get(long vendorId, OFFER_TYPE type, long itemId){
        EntityJoin<Long,BDBOffer> join = new EntityJoin<>(indexById);
        join.addCondition(indexByVendorId, vendorId);
        join.addCondition(indexByType, type);
        join.addCondition(indexByItemId, itemId);
        BDBOffer entity;
        try (ForwardCursor<BDBOffer> cursor = join.entities())
        {
            entity = cursor.next();
        }
        return entity != null ? convertFunc.apply(entity) : null;
    }

    public boolean has(long vendorId, OFFER_TYPE type, long itemId){
        EntityJoin<Long,BDBOffer> join = new EntityJoin<>(indexById);
        join.addCondition(indexByVendorId, vendorId);
        join.addCondition(indexByType, type);
        join.addCondition(indexByItemId, itemId);
        return join.keys().next() != null;
    }

    public Collection<T> getAllByType(long vendorId, OFFER_TYPE type){
        EntityJoin<Long,BDBOffer> join = new EntityJoin<>(indexById);
        join.addCondition(indexByVendorId, vendorId);
        join.addCondition(indexByType, type);
        Collection<T> res = new LinkedList<>();
        try (ForwardCursor<BDBOffer> cursor = join.entities())
        {
            for(BDBOffer entity : cursor){
                res.add(convertFunc.apply(entity));
            }
        }
        return res;
    }

    public Collection<T> getAllByItem(long itemId, OFFER_TYPE type){
        EntityJoin<Long,BDBOffer> join = new EntityJoin<>(indexById);
        join.addCondition(indexByItemId, itemId);
        join.addCondition(indexByType, type);
        Collection<T> res = new LinkedList<>();
        try (ForwardCursor<BDBOffer> cursor = join.entities())
        {
            for(BDBOffer entity : cursor){
                res.add(convertFunc.apply(entity));
            }
        }
        return res;
    }


    public BDBOffer put(BDBOffer offer){
        return indexById.put(offer);
    }

    public void update(BDBOffer offer){
        indexById.putNoReturn(offer);
    }

    public void delete(BDBOffer offer){
        indexById.delete(offer.getId());
    }
}

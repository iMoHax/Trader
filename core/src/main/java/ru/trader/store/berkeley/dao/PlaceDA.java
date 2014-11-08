package ru.trader.store.berkeley.dao;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import ru.trader.store.berkeley.entities.BDBPlace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

public class PlaceDA<T> {
    private final PrimaryIndex<Long, BDBPlace> indexById;
    private final SecondaryIndex<Double, Long, BDBPlace> indexByDistance;
    private final Function<BDBPlace,T> convertFunc;

    public PlaceDA(EntityStore store, Function<BDBPlace, T> convertFunc) {
        this.convertFunc = convertFunc;
        this.indexById = store.getPrimaryIndex(Long.class, BDBPlace.class);
        this.indexByDistance = store.getSecondaryIndex(indexById, Double.class, "distance");
    }

    public T get(long id){
        return DAUtils.get(indexById, id, convertFunc);
    }

    public Collection<T> getAll(){
        return DAUtils.getAll(indexById, convertFunc);
    }

    public Collection<T> getAll(double x, double y, double z, double radius){
        double center = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        Collection<T> res = new LinkedList<>();
        try (EntityCursor<BDBPlace> cursor = indexByDistance.entities(center < radius? 0 : center - radius, true, center + radius, true))
        {
            for(BDBPlace entity : cursor){
                double distance = Math.sqrt(Math.pow(x - entity.getX(), 2) + Math.pow(y - entity.getY(), 2) + Math.pow(z  - entity.getZ(), 2));
                if (distance <= radius)
                    res.add(convertFunc.apply(entity));
            }
        }
        return res;
    }

    public BDBPlace put(BDBPlace place){
        return indexById.put(place);
    }

    public void update(BDBPlace place){
        indexById.putNoReturn(place);
    }

    public void delete(BDBPlace place){
        indexById.delete(place.getId());
    }

}

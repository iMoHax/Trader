package ru.trader.store.berkeley;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.berkeley.dao.*;
import ru.trader.store.berkeley.entities.BDBGroup;

import java.io.File;

public class BDBStore {
    private static final Logger LOG = LoggerFactory.getLogger(BDBStore.class);

    private final Environment dbEnvironment;
    private final EntityStore store;
    private final VendorDA<Vendor> vDA;
    private final OfferDA<Offer> oDA;
    private final ItemDA<Item> iDA;
    private final GroupDA<Group> gDA;
    private final PlaceDA<Place> pDA;

    public BDBStore(String path){
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setSharedCache(true);
        envConfig.setTransactional(true);
        dbEnvironment = new Environment(new File(path), envConfig);

        try {
            StoreConfig dbConfig = new StoreConfig();
            dbConfig.setAllowCreate(true);
            store = new EntityStore(dbEnvironment, "TraderStore", dbConfig);
            vDA = new VendorDA<>(store, v -> new VendorProxy(v, this));
            oDA = new OfferDA<>(store, o -> new OfferProxy(o, this));
            iDA = new ItemDA<>(store, i -> new ItemProxy(i, this));
            gDA = new GroupDA<>(store, g -> g);
            pDA = new PlaceDA<>(store, p -> new PlaceProxy(p, this));

        } catch (DatabaseException e){
            LOG.error("Error on open DB, path {}", path);
            LOG.error("",e);
            throw e;
        }
    }

    public VendorDA<Vendor> getVendorAccessor() {
        return vDA;
    }

    public OfferDA<Offer> getOfferAccessor() {
        return oDA;
    }

    public ItemDA<Item> getItemAccessor() {
        return iDA;
    }

    public GroupDA<Group> getGroupAccessor() {
        return gDA;
    }

    public PlaceDA<Place> getPlaceAccessor() {
        return pDA;
    }


    public void close(){
        if (store != null) {
            try {
                store.close();
            } catch (DatabaseException e){
                LOG.error("Error on close DB", e);
            }
        }
        if (dbEnvironment != null) {
            try {
                dbEnvironment.cleanLog();
                dbEnvironment.close();
            } catch (DatabaseException e){
                LOG.error("Error on close DB environment", e);
            }
        }
    }
}

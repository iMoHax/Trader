package ru.trader.store.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class BDBStore {
    private static final Logger LOG = LoggerFactory.getLogger(BDBStore.class);

    private final Environment dbEnvironment;
    private final Database db;

    public BDBStore(String path){
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setSharedCache(true);
        envConfig.setTransactional(true);
        dbEnvironment = new Environment(new File(path), envConfig);

        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            db = dbEnvironment.openDatabase(null, "Trader", dbConfig);
        } catch (DatabaseException e){
            LOG.error("Error on open DB, path {}", path);
            LOG.error("",e);
            throw e;
        }
    }


    public void close(){
        if (db != null) {
            try {
                db.close();
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

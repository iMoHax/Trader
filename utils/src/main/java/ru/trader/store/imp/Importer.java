package ru.trader.store.imp;

import ru.trader.core.Market;
import ru.trader.store.imp.entities.StarSystemData;

import java.util.EnumSet;

public interface Importer {


    void addFlag(IMPORT_FLAG flag);
    void removeFlag(IMPORT_FLAG flag);
    void setFlags(EnumSet<IMPORT_FLAG> flags);


    boolean next();
    StarSystemData getSystem();

    void imp(Market market);


}

package ru.trader.store.imp;

import ru.trader.core.Market;
import ru.trader.store.imp.entities.StarSystemData;

import java.io.IOException;
import java.util.EnumSet;

public interface Importer {


    void addFlag(IMPORT_FLAG flag);
    void removeFlag(IMPORT_FLAG flag);
    void setFlags(EnumSet<IMPORT_FLAG> flags);

    void cancel();
    boolean next() throws IOException;
    StarSystemData getSystem();

    void imp(Market market) throws IOException;


}

package ru.trader.store.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.store.imp.entities.StarSystemData;
import ru.trader.store.imp.entities.StationData;

import java.io.IOException;
import java.util.Collection;

public class SimpleImporter extends AbstractImporter {
    private final static Logger LOG = LoggerFactory.getLogger(SimpleImporter.class);

    @Override
    protected void before() throws IOException {
    }

    @Override
    protected void after() throws IOException {
    }

    @Override
    public boolean next() throws IOException {
        throw new UnsupportedOperationException("Is SimpleImporter, next() unsupported, use importStation or importSystem");
    }

    @Override
    public StarSystemData getSystem() {
        throw new UnsupportedOperationException("Is SimpleImporter, getSystem() unsupported, use importStation or importSystem");
    }

    public Vendor importStation(Market market, StarSystemData importData){
        Place system = impSystem(market, importData);
        if (system != null) {
            Collection<StationData> stations = importData.getStations();
            if (stations == null || stations.isEmpty()){
                LOG.warn("Station data not found");
                return null;
            }
            StationData stationData = stations.iterator().next();
            return impStation(system, stationData);
        } else {
            LOG.warn("System {} not found", importData.getName());
            return null;
        }
    }

    public Place importSystem(Market market, StarSystemData importData){
        return impSystem(market, importData);
    }

}

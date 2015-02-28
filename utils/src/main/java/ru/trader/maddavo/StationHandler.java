package ru.trader.maddavo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.core.Place;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;

public class StationHandler extends CSVParseHandler {
    private final static Logger LOG = LoggerFactory.getLogger(StationHandler.class);

    protected StationHandler(Market market) {
        super(market);
    }

    @Override
    protected void doWork(String[] values) {
        //unq:name@System.system_id,unq:name,ls_from_star,blackmarket,max_pad_size,market,shipyard,modified
        Place system = market.get(values[0]);
        if (system == null){
            LOG.warn("Not found system {}", values[0]);
            return;
        }
        double distance = 0;
        try {
            distance = Double.valueOf(values[2]);
        } catch (NumberFormatException ex){
            LOG.warn("Distance {} - is not correct", values[2]);
        }

        Collection<SERVICE_TYPE> adding = new ArrayList<>();
        Collection<SERVICE_TYPE> removing = new ArrayList<>();

        if ("Y".equals(values[3])) adding.add(SERVICE_TYPE.BLACK_MARKET);
        if ("N".equals(values[3])) removing.add(SERVICE_TYPE.BLACK_MARKET);

        if ("M".equals(values[4])) {adding.add(SERVICE_TYPE.MEDIUM_LANDPAD); removing.add(SERVICE_TYPE.LARGE_LANDPAD);}
        if ("L".equals(values[4])) {adding.add(SERVICE_TYPE.MEDIUM_LANDPAD); adding.add(SERVICE_TYPE.LARGE_LANDPAD);}

        if ("Y".equals(values[5])) adding.add(SERVICE_TYPE.MARKET);
        if ("N".equals(values[5])) removing.add(SERVICE_TYPE.MARKET);

        if ("Y".equals(values[6])) adding.add(SERVICE_TYPE.SHIPYARD);
        if ("N".equals(values[6])) removing.add(SERVICE_TYPE.SHIPYARD);

        updateStation(system, values[1], distance, adding, removing);
    }

    private void updateStation(Place system, String name, double distance, Collection<SERVICE_TYPE> addServices, Collection<SERVICE_TYPE> removeServices){
        Vendor station = system.get(name);
        if (station == null){
            LOG.debug("{} - is new station, adding", name);
            station = system.addVendor(name);
        }
        if (distance > 0 && distance != station.getDistance()) {
            station.setDistance(distance);
        }

        addServices.forEach(station::add);
        removeServices.forEach(station::remove);
    }
}

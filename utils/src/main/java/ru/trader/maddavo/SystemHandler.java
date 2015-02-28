package ru.trader.maddavo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.core.Place;


public class SystemHandler extends CSVParseHandler {
    private final static Logger LOG = LoggerFactory.getLogger(SystemHandler.class);

    protected SystemHandler(Market market) {
        super(market);
    }

    @Override
    protected void doWork(String[] values) {
        //unq:name,pos_x,pos_y,pos_z,name@Added.added_id,modified

        double x = Double.NaN, y = Double.NaN, z = Double.NaN;
        try {
            x = Double.valueOf(values[1]);
            y = Double.valueOf(values[2]);
            z = Double.valueOf(values[3]);

        } catch (NumberFormatException ex){
            LOG.warn("Position {}, {}, {} - is not correct", values[1], values[2], values[3]);
        }

        updateSystem(market, values[0], x, y, z);
    }

    private void updateSystem(Market market, String name, double x, double y, double z){
        Place system = market.get(name);
        if (system == null){
            LOG.debug("{} - is new system, adding", name);
            market.addPlace(name, x, y, z);
        } else {
            if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z) &&
                (x != system.getX() || y != system.getY() || z != system.getZ())){
                system.setPosition(x, y, z);
            }
        }
    }
}

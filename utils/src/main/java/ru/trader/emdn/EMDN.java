package ru.trader.emdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class EMDN {
    private final static Logger LOG = LoggerFactory.getLogger(EMDN.class);

    private final String subServer;
    private final Market cache = new Market();
    private ZMQ.Context context = null;
    private ZMQ.Socket subscriber = null;
    private ScheduledExecutorService executor;
    private boolean clear;

    public EMDN(String subServer, boolean clearOnShutdown) {
        this.subServer = subServer;
        clear = clearOnShutdown;
    }

    private void init(){
        context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
    }

    public void start(){
        if (subscriber!=null) shutdown();
        init();
        LOG.info("Connect to server {}", subServer);
        subscriber.connect(subServer);
        LOG.trace("Subscribe");
        subscriber.subscribe(new byte[0]);
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() -> {
            try {
                byte[] receivedData = subscriber.recv(0);
                LOG.trace("Received data: {}", receivedData);
                if (receivedData == null) return;
                //receivedData = decompress(receivedData);
                String market_csv = new String(receivedData, "UTF-8");
                parseCSV(market_csv);
            } catch (ZMQException | UnsupportedEncodingException ex) {
                if (!executor.isShutdown())
                    LOG.error("Error on get data from EMDN", ex);
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (subscriber!=null){
            LOG.info("Shutdown EMDN client");
            executor.shutdown();
            subscriber.close();
            context.term();
        }
        subscriber = null;
        context = null;
        if (clear)
            cache.clear();
    }


    private void parseCSV(String csv) {
        LOG.debug("Parse csv: {}", csv);
        if (csv.isEmpty()) return;
        String[] flds = csv.split(",");
        // buyPrice,sellPrice,demand,demandLevel,stationStock,stationStockLevel,categoryName,itemName,stationName,timestamp
        ItemData item = new ItemData(flds[7]);
        item.setBuy(Double.valueOf(flds[0]), Long.valueOf(flds[4]));
        item.setSell(Double.valueOf(flds[1]), Long.valueOf(flds[2]));
        LOG.trace("Item: {}", item);
        String stName = flds[8].split("\\(")[0].trim();
        LOG.trace("Station: {}", stName);
        Station station = cache.getVendor(stName);
        if (station != null){
            LOG.trace("Is old, update");
            station.update(item);
        } else {
            LOG.trace("Is new, create");
            station = new Station(stName);
            station.update(item);
            cache.addVendor(station);
        }
    }

    private byte[] decompress(byte[] input){
        byte[] decompressed = new byte[input.length * 16];
        int decompressedLength = 0;
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(input);
            decompressedLength = inflater.inflate(decompressed);
        } catch (DataFormatException e) {
            LOG.error("Error on decompress raw data {}", input);
            LOG.error("",e);
        } finally {
            inflater.end();
        }
        byte[] res = new byte[decompressedLength];
        System.arraycopy(decompressed, 0, res, 0, decompressedLength);
        return res;
    }

    public Station getVendor(String name){
        return cache.getVendor(name);
    }

}

package ru.trader.emdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class EMDN {
    private final static Logger LOG = LoggerFactory.getLogger(EMDN.class);

    private final ConcurrentHashMap<String, Station> cache = new ConcurrentHashMap<>(40, 0.9f, 1);
    private final ZMQ.Context context;
    private ExecutorService executor;
    private String subServer;
    private Future<?> receive;
    private boolean clear;

    public EMDN() {
        context = ZMQ.context(1);
    }

    public EMDN(String subServer, boolean clearOnShutdown) {
        this();
        this.subServer = subServer;
        clear = clearOnShutdown;
    }


    public void start(){
        if (isActive()) return;
        if (executor == null) executor = Executors.newSingleThreadExecutor();
        receive = executor.submit(new Receiver());
    }


    public void stop() {
        if (isActive()){
            LOG.info("Stop EMDN client");
            receive.cancel(false);
            receive = null;
            if (clear)
                cache.clear();
        }
    }

    public void shutdown() {
        LOG.info("Shutdown EMDN client");
        stop();
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            }
        }
        context.term();
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

    public Station get(String name){
        return cache.get(name);
    }

    public Station pop(String name) {
        return cache.remove(name);
    }

    public boolean isActive(){
        return receive!=null;
    }

    public void connectTo(String subServer){
        if (subServer.equals(this.subServer)) return;
        boolean active = isActive();
        if (active) stop();
        this.subServer = subServer;
        if (active) start();
    }

    private class Receiver implements Runnable {

        @Override
        public void run() {
            try (ZMQ.Socket subscriber = context.socket(ZMQ.SUB)){
                subscriber.setReceiveTimeOut(10000);
                LOG.info("Connect to server {}", subServer);
                subscriber.connect(subServer);
                LOG.trace("Subscribe");
                subscriber.subscribe(new byte[0]);
                Station station = null;
                while (!executor.isShutdown() && !receive.isCancelled()){
                    try {
                        byte[] receivedData = subscriber.recv(0);
                        LOG.trace("Received data: {}", receivedData);
                        if (receivedData == null) continue;
                        //receivedData = decompress(receivedData);
                        String market_csv = new String(receivedData, "UTF-8");
                        station = parseCSV(market_csv, station);
                        if (!subscriber.hasReceiveMore()){
                            cache.put(station.getName(), station);
                            station = null;
                        }
                    } catch (ZMQException | UnsupportedEncodingException ex) {
                        LOG.error("Error on get data from EMDN", ex);
                    }
                }
            } catch (Exception ex){
                LOG.error("Error on connect to EMDN", ex);
            }
        }

        private Station parseCSV(String csv, Station station) {
            LOG.debug("Parse csv: {}", csv);
            if (!csv.isEmpty()) {
                String[] flds = csv.split(",");
                // buyPrice,sellPrice,demand,demandLevel,stationStock,stationStockLevel,categoryName,itemName,stationName,timestamp
                ItemData item = new ItemData(flds[7]);
                item.setBuy(Double.valueOf(flds[0]), Long.valueOf(flds[4]));
                item.setSell(Double.valueOf(flds[1]), Long.valueOf(flds[2]));
                LOG.trace("Item: {}", item);
                String stName = flds[8].split("\\(")[0].trim();
                LOG.trace("Station: {}", stName);
                if (station == null) {
                    LOG.trace("Is new, create");
                    station = new Station(stName);
                }
                assert stName.equals(station.getName());
                station.update(item);
            }
            return station;
        }

    }
}

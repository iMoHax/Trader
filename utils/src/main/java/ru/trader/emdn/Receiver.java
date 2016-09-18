package ru.trader.emdn;

import com.fasterxml.jackson.core.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import ru.trader.emdn.entities.Message;

import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Receiver implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(Receiver.class);
    private final ZMQ.Context context;
    private final EMDNParser parser;
    private String server;
    private boolean run;
    private Consumer<Message> consumer;

    public Receiver(ZMQ.Context context, String server) {
        this.context = context;
        this.server = server;
        parser = new EMDNParser();
        run = false;
        consumer = null;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void cancel(){
        run = false;
    }

    public boolean isActive(){
        return run;
    }

    protected void onMessage(Message message){
        if (consumer != null){
            consumer.accept(message);
        }
    }

    public void setConsumer(Consumer<Message> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void run() {
        run = true;
        try (ZMQ.Socket subscriber = context.socket(ZMQ.SUB)){
            LOG.info("Connect to server {}", server);
            subscriber.connect(server);
            LOG.trace("Subscribe");
            subscriber.subscribe("".getBytes());
            StringBuilder builder = new StringBuilder();
            ZMQ.Poller poller = new ZMQ.Poller(1);
            poller.register(subscriber, ZMQ.Poller.POLLIN);

            while (run){
                try {
                    poller.poll(10000);
                    if (poller.pollin(0)){
                        byte[] receivedData = subscriber.recv(ZMQ.PAIR);
                        LOG.trace("Received data: {}", receivedData);
                        if (receivedData == null) continue;
                        receivedData = decompress(receivedData);
                        String marketJson = new String(receivedData, "UTF-8");
                        LOG.trace("Decompress message: {}", marketJson);
                        builder.append(marketJson);
                        if (!subscriber.hasReceiveMore()){
                            Message message = parser.parse(builder.toString());
                            LOG.trace("Parsed message: {}", message);
                            builder.setLength(0);
                            onMessage(message);
                        }
                    }
                } catch (ZMQException | JsonParseException | UnsupportedEncodingException ex) {
                    LOG.error("Error on get data from EMDN", ex);
                }
            }
        } catch (Exception ex){
            LOG.error("Error on connect to EMDN", ex);
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

}

package ru.trader.emdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import ru.trader.emdn.entities.Message;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class EMDN {
    private final static Logger LOG = LoggerFactory.getLogger(EMDN.class);

    private final ZMQ.Context context;
    private ExecutorService executor;
    private Receiver receiver;
    private Future<?> receiverFuture;

    public EMDN(){
        this("tcp://eddn-relay.elite-markets.net:9500",null);
    }

    public EMDN(String subServer, Consumer<Message> consumer) {
        context = ZMQ.context(1);
        receiver = new Receiver(context, subServer);
        receiver.setConsumer(consumer);
    }


    public void start(){
        if (isActive()) return;
        LOG.info("Start EMDN client");
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        receiverFuture = executor.submit(receiver);
    }


    public void stop() {
        if (isActive()){
            LOG.info("Stop EMDN client");
            receiver.cancel();
            receiverFuture.cancel(false);
            receiverFuture = null;
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

    public boolean isActive(){
        return receiverFuture != null && receiver.isActive();
    }

    public void connectTo(String subServer){
        if (subServer.equals(receiver.getServer())) return;
        boolean active = isActive();
        if (active) stop();
        receiver.setServer(subServer);
        if (active) start();
    }

}

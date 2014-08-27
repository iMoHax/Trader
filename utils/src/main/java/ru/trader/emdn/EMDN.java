package ru.trader.emdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class EMDN {
    private final static Logger LOG = LoggerFactory.getLogger(EMDN.class);

    private final String subServer;

    public EMDN(String subServer) {
        this.subServer = subServer;
    }


    public void getData(){
        ZMQ.Context context = ZMQ.context(1);
        try (ZMQ.Socket socket = context.socket(ZMQ.SUB)) {
            socket.setReceiveTimeOut(10000);
            LOG.debug("Connect to server {}", subServer);
            socket.connect(subServer);

            LOG.trace("Subscribe");
            socket.subscribe(new byte[0]);
            for (int i = 0; i < 5; i++) {
                try {
                    byte[] receivedData = socket.recv(0);
                    LOG.trace("Recived data: {}", receivedData);
                    if (receivedData == null) continue;
                    String market_json = new String(decompress(receivedData), "UTF-8");
                    LOG.trace("JSON: {}", market_json);
                } catch (ZMQException | UnsupportedEncodingException ex) {
                    LOG.error("Error on get data from EMDN", ex);
                }
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
}

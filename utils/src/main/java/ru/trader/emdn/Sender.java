package ru.trader.emdn;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.edce.entities.EDPacket;
import ru.trader.emdn.entities.Converter;

import java.io.IOException;

public class Sender {
    private final static Logger LOG = LoggerFactory.getLogger(Sender.class);
    private final CloseableHttpClient httpClient;
    private final Converter converter;
    private String url;

    public Sender(String url) {
        this(url, null);
    }

    public Sender(String url, String proxyServer, int port) {
        this(url, new HttpHost(proxyServer,port));
    }

    public Sender(String url, HttpHost proxy) {
        this.url = url;
        httpClient = createClient(proxy);
        converter = new Converter();
    }

    private CloseableHttpClient createClient(HttpHost proxy){
        HttpClientBuilder builder = HttpClients.custom();
        if (proxy != null){
            builder.setProxy(proxy);
        }
        return builder.build();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean send(EDPacket edcePack, String soft, String version){
        String json;
        try {
            json = converter.convertToCommodity(edcePack, soft, version);
        } catch (IOException e) {
            LOG.warn("Error on convert EDCE packet to EDDN:", e);
            return false;
        }
        return send(json);
    }


    private boolean send(String json){
        HttpPost httpPost = new HttpPost(url);
        try {
            StringEntity stringEntity = new StringEntity(json);
            httpPost.setEntity(stringEntity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
            return httpClient.execute(httpPost, new AbstractResponseHandler<Boolean>() {
                @Override
                public Boolean handleEntity(HttpEntity entity) throws IOException {
                    return true;
                }
            });
        } catch (IOException e) {
            LOG.error("Error on send JSON to {}:", url);
            LOG.error("", e);
            return false;
        }
    }




    public void close() throws IOException {
        httpClient.close();
    }



}

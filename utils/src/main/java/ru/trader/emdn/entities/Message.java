package ru.trader.emdn.entities;

import com.fasterxml.jackson.databind.JsonNode;
import ru.trader.store.imp.entities.StarSystemData;

public class Message {
    private SUPPORT_VERSIONS version;
    private Header header;
    private JsonNode body;

    public Message(SUPPORT_VERSIONS version, Header header, JsonNode body) {
        this.version = version;
        this.header = header;
        this.body = body;
    }

    public SUPPORT_VERSIONS getVersion() {
        return version;
    }

    public Header getHeader() {
        return header;
    }

    public StarSystemData getImportData(){
        return new EDDNSystemData(body, version);

    }


    @Override
    public String toString() {
        return "Message{" +
                "version='" + version + '\'' +
                ", header=" + header +
                ", body=" + body +
                "} ";
    }
}

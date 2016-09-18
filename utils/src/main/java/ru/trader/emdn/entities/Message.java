package ru.trader.emdn.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("$schemaRef")
    private String schemaRef;

    private Header header;

    private Body body;

    public Message(String schemaRef, Header header, Body body) {
        this.schemaRef = schemaRef;
        this.header = header;
        this.body = body;
    }

    public String getSchemaRef() {
        return schemaRef;
    }

    public Header getHeader() {
        return header;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "schemaRef='" + schemaRef + '\'' +
                ", header=" + header +
                ", body=" + body +
                "} ";
    }
}

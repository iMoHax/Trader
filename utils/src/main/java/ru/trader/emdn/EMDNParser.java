package ru.trader.emdn;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.emdn.entities.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EMDNParser {
    private final static Logger LOG = LoggerFactory.getLogger(EMDNParser.class);

    private final ObjectMapper mapper;
    private final static ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    public EMDNParser() {
        this(DEFAULT_MAPPER);
    }

    public EMDNParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Nullable
    public Message parse(String json) throws IOException {
        JsonParser parser = mapper.getFactory().createParser(json);
        JsonNode node = parser.readValueAsTree();
        String schema = node.get("$schemaRef").asText("");
        SUPPORT_VERSIONS version = SUPPORT_VERSIONS.getVersion(schema);
        if (version == null){
            LOG.warn("Unknown EDDN message schema {}", schema);
            return null;
        }
        return parse(node, version);
    }

    protected Message parse(JsonNode node, SUPPORT_VERSIONS version){
        Header header = parseHeader(node.get("header"));
        JsonNode body = node.get("message");
        if (header == null || body == null) return null;
        return new Message(version, header, body);
    }

    protected Header parseHeader(JsonNode node){
        if (node == null){
            LOG.warn("Not found header on parse EDDN message");
            return null;
        }
        JsonNode uploaderID = node.get("uploaderID");
        JsonNode softwareName = node.get("softwareName");
        JsonNode softwareVersion = node.get("softwareVersion");
        JsonNode gatewayTimestamp = node.get("gatewayTimestamp");
        if (uploaderID == null || softwareName == null || softwareVersion == null){
            LOG.warn("Header EDDN message don't have required fields");
            return null;
        }
        Header header = new Header(uploaderID.asText(), softwareName.asText(), softwareVersion.asText());
        if (gatewayTimestamp != null){
            LocalDateTime dt = LocalDateTime.parse(gatewayTimestamp.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            header.setGatewayTimestamp(dt);
        }
        return header;
    }

}

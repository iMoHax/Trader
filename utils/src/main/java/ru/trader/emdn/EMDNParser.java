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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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
        String schema = node.get("$schemaRef").asText();
        PARSERS p = PARSERS.getParser(schema);
        if (p == null){
            LOG.warn("Unknown EDDN message schema {}", schema);
            return null;
        }
        return p.parse(node);
    }

    private enum PARSERS {
        V1_SHIPYARD("http://schemas.elite-markets.net/eddn/shipyard/1"),
        V2_SHIPYARD("http://schemas.elite-markets.net/eddn/shipyard/2"),
        V1("http://schemas.elite-markets.net/eddn/commodity/1"){
            @Override
            protected Body parseBody(JsonNode node) {
                if (node == null){
                    LOG.warn("Not found message body on parse EDDN message");
                    return null;
                }
                JsonNode systemName = node.get("systemName");
                JsonNode stationName = node.get("stationName");
                JsonNode timestamp = node.get("timestamp");
                if (systemName == null || stationName == null || timestamp == null){
                    LOG.warn("Body EDDN message don't have required fields");
                    return null;
                }
                StarSystem starSystem = new StarSystem(systemName.asText());
                Station station = new Station(stationName.asText());
                LocalDateTime dt = LocalDateTime.parse(timestamp.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                Body body = new Body(starSystem, station);
                body.setTimestamp(dt);
                body.addAll(parseCommodities(node));
                return body;
            }

            @Override
            protected Collection<Item> parseCommodities(JsonNode node) {
                JsonNode name = node.get("itemName");
                JsonNode buyPrice = node.get("buyPrice");
                JsonNode supply = node.get("stationStock");
                JsonNode supplyLevel = node.get("supplyLevel");
                JsonNode sellPrice = node.get("sellPrice");
                JsonNode demand = node.get("demand");
                JsonNode demandLevel = node.get("demandLevel");
                if (name == null || buyPrice == null || supply == null || sellPrice == null || demand == null){
                    LOG.warn("Commodity of EDDN message don't have required fields");
                    return null;
                }
                Item item = new Item(name.asText(), buyPrice.asLong(), supply.asLong(), sellPrice.asLong(), demand.asLong());
                if (supplyLevel != null){
                    item.setSupplyLevel(LEVEL_TYPE.fromJSON(supplyLevel.asText()));
                }
                if (demandLevel != null){
                    item.setDemandLevel(LEVEL_TYPE.fromJSON(demandLevel.asText()));
                }
                return Collections.singleton(item);
            }
        },

        V2("http://schemas.elite-markets.net/eddn/commodity/2"),

        V3("http://schemas.elite-markets.net/eddn/commodity/3"){
            @Override
            protected Body parseBody(JsonNode node) {
                if (node == null){
                    LOG.warn("Not found message body on parse EDDN message");
                    return null;
                }
                JsonNode systemNode = node.get("system");
                JsonNode stationNode = node.get("station");
                JsonNode timestamp = node.get("timestamp");
                JsonNode commodities = node.get("commodities");
                if (systemNode == null || stationNode == null || timestamp == null || commodities == null){
                    LOG.warn("Body EDDN message don't have required fields");
                    return null;
                }
                StarSystem starSystem = parseSystem(systemNode);
                Station station = parseStation(stationNode);
                LocalDateTime dt = LocalDateTime.parse(timestamp.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                Body body = new Body(starSystem, station);
                body.setTimestamp(dt);
                body.addAll(parseCommodities(commodities));
                return body;
            }

            private Station parseStation(JsonNode node) {
                JsonNode name = node.get("name");
                JsonNode id = node.get("id");
                if (name == null){
                    LOG.warn("Station in EDDN message don't have required fields");
                    return null;
                }
                Station station = new Station(name.asText());
                if (id != null){
                    station.setId(id.asLong());
                }
                return station;
            }

            private StarSystem parseSystem(JsonNode node) {
                JsonNode name = node.get("name");
                JsonNode id = node.get("id");
                JsonNode address = node.get("address");
                if (name == null){
                    LOG.warn("System in EDDN message don't have required fields");
                    return null;
                }
                StarSystem system = new StarSystem(name.asText());
                if (id != null){
                    system.setId(id.asLong());
                }
                if (address != null){
                    system.setAddress(address.asLong());
                }
                return system;
            }


        };

        private final String schema;

        public Message parse(JsonNode node){
            Header header = parseHeader(node.get("header"));
            Body body = parseBody(node.get("message"));
            if (header == null || body == null) return null;
            return new Message(schema, header, body);
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

        protected Body parseBody(JsonNode node){
            if (node == null){
                LOG.warn("Not found message body on parse EDDN message");
                return null;
            }
            JsonNode systemName = node.get("systemName");
            JsonNode stationName = node.get("stationName");
            JsonNode timestamp = node.get("timestamp");
            JsonNode commodities = node.get("commodities");
            JsonNode ships = node.get("ships");
            if (systemName == null || stationName == null || timestamp == null || (commodities == null && ships == null)){
                LOG.warn("Body EDDN message don't have required fields");
                return null;
            }
            StarSystem starSystem = new StarSystem(systemName.asText());
            Station station = new Station(stationName.asText());
            LocalDateTime dt = LocalDateTime.parse(timestamp.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            Body body = new Body(starSystem, station);
            body.setTimestamp(dt);
            if (commodities != null){
                body.addAll(parseCommodities(commodities));
            }
            if (ships != null){
                body.addShips(parseShips(ships));
            }
            return body;
        }

        protected Collection<Item> parseCommodities(JsonNode commodities) {
            Collection<Item> res = new ArrayList<>();
            for (Iterator<JsonNode> iterator = commodities.elements(); iterator.hasNext(); ) {
                JsonNode node = iterator.next();
                JsonNode name = node.get("name");
                JsonNode id = node.get("id");
                JsonNode buyPrice = node.get("buyPrice");
                JsonNode supply = node.get("supply");
                JsonNode supplyLevel = node.get("supplyLevel");
                JsonNode sellPrice = node.get("sellPrice");
                JsonNode demand = node.get("demand");
                JsonNode demandLevel = node.get("demandLevel");
                if (name == null || buyPrice == null || supply == null || sellPrice == null || demand == null){
                    LOG.warn("Commodity of EDDN message don't have required fields");
                    return null;
                }
                Item item = new Item(name.asText(), buyPrice.asLong(), supply.asLong(), sellPrice.asLong(), demand.asLong());
                if (id != null){
                    item.setId(id.asLong());
                }
                if (supplyLevel != null){
                    item.setSupplyLevel(LEVEL_TYPE.fromJSON(supplyLevel.asText()));
                }
                if (demandLevel != null){
                    item.setDemandLevel(LEVEL_TYPE.fromJSON(demandLevel.asText()));
                }
                res.add(item);
            }
            return res;
        }

        protected Collection<Ship> parseShips(JsonNode ships) {
            Collection<Ship> res = new ArrayList<>();
            for (Iterator<JsonNode> iterator = ships.elements(); iterator.hasNext(); ) {
                JsonNode node = iterator.next();
                String name = node.asText();
                Ship ship = new Ship(name);
                res.add(ship);
            }
            return res;
        }

        private PARSERS(String schema) {
            this.schema = schema;
        }

        public static PARSERS getParser(String schema){
            for (PARSERS parser : PARSERS.values()) {
                if (parser.schema.equals(schema)) return parser;
            }
            return null;
        }
    }

}

package ru.trader.store.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class FiltersStore {
    private static final Logger LOG = LoggerFactory.getLogger(FiltersStore.class);
    private static final String DISABLE_FIELD = "disable";
    private static final String CENTER_FIELD = "center";
    private static final String RADIUS_FIELD = "radius";
    private static final String DISTANCE_FIELD = "distance";
    private static final String TYPES_FIELD = "types";
    private static final String SERVICES_FIELD = "services";
    private static final String FACTIONS_FIELD = "factions";
    private static final String GOVERNMENTS_FIELD = "governments";
    private static final String EXCLUDE_VENDORS_FIELD = "excludes";
    private static final String SYSTEM_FIELD = "system";
    private static final String STATION_FIELD = "station";
    private static final String DEFAULT_VENDOR_FILTER_KEY = "default";
    private static final String VENDOR_FILTERS_FIELD = "stations";
    private static final String LEGAL_ONLY_FIELD = "legalOnly";
    private static final String DONT_SELL_FIELD = "notSell";
    private static final String DONT_BUY_FIELD = "notBuy";
    private static final String SELL_FILTER_FIELD = "excludeSell";
    private static final String BUY_FILTER_FIELD = "excludeBuy";

    private final ObjectMapper objectMapper;

    public FiltersStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(MarketFilter filter, File file) throws IOException {
        try (JsonGenerator generator = objectMapper.getFactory().createGenerator(file, JsonEncoding.UTF8)){
            write(filter, generator);
            generator.flush();
        }
    }

    private void write(MarketFilter filter, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        Place center = filter.getCenter();
        generator.writeStringField(CENTER_FIELD, center != null ? center.getName() : "");
        generator.writeNumberField(RADIUS_FIELD, filter.getRadius());
        generator.writeNumberField(DISTANCE_FIELD, filter.getDistance());
        generator.writeArrayFieldStart(TYPES_FIELD);
        for (STATION_TYPE stationType : filter.getTypes()) {
            generator.writeString(stationType.toString());
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart(SERVICES_FIELD);
        for (SERVICE_TYPE service : filter.getServices()) {
            generator.writeString(service.toString());
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart(FACTIONS_FIELD);
        for (FACTION faction : filter.getFactions()) {
            generator.writeString(faction.toString());
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart(GOVERNMENTS_FIELD);
        for (GOVERNMENT government : filter.getGovernments()) {
            generator.writeString(government.toString());
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart(EXCLUDE_VENDORS_FIELD);
        for (Vendor vendor : filter.getExcludes()) {
            generator.writeStartObject();
            generator.writeStringField(SYSTEM_FIELD, vendor.getPlace().getName());
            generator.writeStringField(STATION_FIELD, vendor.getName());
            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeObjectFieldStart(VENDOR_FILTERS_FIELD);
        write(DEFAULT_VENDOR_FILTER_KEY, filter.getDefaultVendorFilter(), generator);
        for (Map.Entry<String, VendorFilter> entry : filter.getVendorFilters().entrySet()) {
            write(entry.getKey(), entry.getValue(), generator);
        }
        generator.writeEndObject();

        generator.writeEndObject();
    }

    private void write(String key, VendorFilter filter, JsonGenerator generator) throws IOException {
        generator.writeObjectFieldStart(key);
        generator.writeBooleanField(DISABLE_FIELD, filter.isDisable());
        generator.writeBooleanField(LEGAL_ONLY_FIELD, filter.isSkipIllegal());
        generator.writeBooleanField(DONT_SELL_FIELD, filter.isDontSell());
        generator.writeBooleanField(DONT_BUY_FIELD, filter.isDontBuy());
        generator.writeArrayFieldStart(SELL_FILTER_FIELD);
        for (Item item : filter.getSellExcludes()) {
            generator.writeString(item.getName());
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart(BUY_FILTER_FIELD);
        for (Item item : filter.getBuyExcludes()) {
            generator.writeString(item.getName());
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    public MarketFilter read(Market market, File file) throws IOException {
        MarketFilter filter = new MarketFilter();
        if (!file.exists()){
            LOG.warn("Don't exists filters file {}", file);
        } else {
            try (JsonParser parser = objectMapper.getFactory().createParser(file)){
                read(filter, market, parser);
            }
        }
        return filter;
    }


    private void read(MarketFilter filter, Market market, JsonParser parser) throws IOException {
        JsonNode node = parser.readValueAsTree();
        String placeName = node.get(CENTER_FIELD).asText();
        if (!placeName.isEmpty()){
            Place place = market.get(placeName);
            if (place != null){
                filter.setCenter(place);
            } else {
                LOG.warn("Parse filter error: Don't found system {}", placeName);
            }
        }
        filter.setRadius(node.get(RADIUS_FIELD).asDouble());
        filter.setDistance(node.get(DISTANCE_FIELD).asDouble());
        Iterator<JsonNode> iterator = node.get(TYPES_FIELD).elements();
        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            STATION_TYPE value = STATION_TYPE.valueOf(n.asText());
            filter.add(value);
        }
        iterator = node.get(SERVICES_FIELD).elements();
        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            SERVICE_TYPE value = SERVICE_TYPE.valueOf(n.asText());
            filter.add(value);
        }
        iterator = node.get(FACTIONS_FIELD).elements();
        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            FACTION value = FACTION.valueOf(n.asText());
            filter.add(value);
        }
        iterator = node.get(GOVERNMENTS_FIELD).elements();
        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            GOVERNMENT value = GOVERNMENT.valueOf(n.asText());
            filter.add(value);
        }
        iterator = node.get(EXCLUDE_VENDORS_FIELD).elements();
        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            placeName = n.get(SYSTEM_FIELD).asText();
            Place place = market.get(placeName);
            if (place == null){
                LOG.warn("Parse filter error: Don't found system {}", placeName);
            } else {
                String vendorName = n.get(STATION_FIELD).asText();
                Vendor vendor = place.get(vendorName);
                if (vendor == null){
                    LOG.warn("Parse filter error: Don't found vendor {} in system {}", vendorName, place);
                } else {
                    filter.addExclude(vendor);
                }
            }
        }
        Iterator<Map.Entry<String,JsonNode>> fi = node.get(VENDOR_FILTERS_FIELD).fields();
        while (fi.hasNext()){
            Map.Entry<String,JsonNode> n = fi.next();
            if (DEFAULT_VENDOR_FILTER_KEY.equals(n.getKey())){
                VendorFilter vendorFilter = filter.getDefaultVendorFilter();
                read(vendorFilter, market, n.getValue());
            } else {
                VendorFilter vendorFilter = new VendorFilter();
                read(vendorFilter, market, n.getValue());
                filter.addFilter(n.getKey(), vendorFilter);
            }
        }
    }

    private void read(VendorFilter filter, Market market, JsonNode node){
        filter.setDisable(node.get(DISABLE_FIELD).asBoolean());
        filter.setSkipIllegal(node.get(LEGAL_ONLY_FIELD).asBoolean());
        filter.dontSell(node.get(DONT_SELL_FIELD).asBoolean());
        filter.dontBuy(node.get(DONT_BUY_FIELD).asBoolean());
        Iterator<JsonNode> iterator = node.get(SELL_FILTER_FIELD).elements();
        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            String itemName = n.asText();
            Item item = market.getItem(itemName);
            if (item == null){
                LOG.warn("Parse filter error: Don't found item {}", itemName);
            } else {
                filter.addSellExclude(item);
            }
        }
        iterator = node.get(BUY_FILTER_FIELD).elements();
        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            String itemName = n.asText();
            Item item = market.getItem(itemName);
            if (item == null){
                LOG.warn("Parse filter error: Don't found item {}", itemName);
            } else {
                filter.addBuyExclude(item);
            }
        }
    }
}

package ru.trader.emdn.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.edce.entities.Commodity;
import ru.trader.edce.entities.EDPacket;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Converter {
    private final static Logger LOG = LoggerFactory.getLogger(Converter.class);

    private final ObjectMapper mapper;
    private final static ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    public Converter() {
        this(DEFAULT_MAPPER);
    }

    public Converter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Nullable
    public String convertToCommodity(EDPacket edce, String soft, String version) throws IOException {
        if (edce.getCommander() == null || edce.getLastSystem() == null || edce.getLastStarport() == null
            || edce.getLastStarport().getCommodities() == null || edce.getLastStarport().getCommodities().isEmpty()){
            return null;
        }

        String uploaderId = edce.getCommander().getName();

        StringWriter writer = new StringWriter();
        try (JsonGenerator generator = mapper.getFactory().createGenerator(writer)){
            generator.writeStartObject();
            generator.writeStringField("$schemaRef", SUPPORT_VERSIONS.V3.getSchema());
            generator.writeObjectFieldStart("header");
            generator.writeStringField("uploaderID", uploaderId);
            generator.writeStringField("softwareName", soft);
            generator.writeStringField("softwareVersion", version);
            generator.writeEndObject(); //header
            generator.writeObjectFieldStart("message");
            generator.writeStringField("systemName", edce.getLastSystem().getName());
            generator.writeStringField("stationName", edce.getLastStarport().getName());
            generator.writeStringField("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            generator.writeArrayFieldStart("commodities");
            boolean hasMarketCommodity = false;
            for (Commodity commodity : edce.getLastStarport().getCommodities()) {
                if ("NonMarketable".equals(commodity.getCategoryname())) continue;
                hasMarketCommodity = true;
                generator.writeStartObject();
                generator.writeStringField("name", commodity.getName());
                generator.writeNumberField("meanPrice", commodity.getMeanPrice());
                generator.writeNumberField("buyPrice", commodity.getBuyPrice());
                generator.writeNumberField("stock", commodity.getRawStock());
                generator.writeNumberField("stockBracket", commodity.getStockBracket());
                generator.writeNumberField("sellPrice", commodity.getSellPrice());
                generator.writeNumberField("demand", commodity.getDemand());
                generator.writeNumberField("demandBracket", commodity.getDemandBracket());
                if (commodity.getStatusFlags() != null && !commodity.getStatusFlags().isEmpty()){
                    generator.writeArrayFieldStart("statusFlags");
                    for (String s : commodity.getStatusFlags()) {
                        generator.writeString(s);
                    }
                    generator.writeEndArray();
                }
                generator.writeEndObject();
            }
            if (!hasMarketCommodity) return null;

            generator.writeEndArray();
            generator.writeEndObject(); //message
            generator.writeEndObject(); //root
            generator.flush();
        }

        return writer.toString();
    }

}

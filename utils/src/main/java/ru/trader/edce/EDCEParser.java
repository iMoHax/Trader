package ru.trader.edce;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.trader.edce.entities.EDPacket;

import java.io.IOException;

public class EDCEParser {


    public static EDPacket parseJSON(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, EDPacket.class);
    }


}

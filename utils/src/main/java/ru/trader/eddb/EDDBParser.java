package ru.trader.eddb;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.eddb.entities.EDDBSystemData;
import ru.trader.store.imp.AbstractImporter;
import ru.trader.store.imp.entities.StarSystemData;

import java.io.File;
import java.io.IOException;

public class EDDBParser extends AbstractImporter {
    private final static Logger LOG = LoggerFactory.getLogger(EDDBParser.class);
    private final FILE_TYPE type;
    private final File file;
    private JsonParser parser;
    private final ObjectMapper mapper;
    private EDDBSystemData next;

    private EDDBParser(File file, FILE_TYPE type) {
        super();
        this.type = type;
        this.file = file;
        this.mapper = new ObjectMapper();
    }


    @Override
    protected void before() throws IOException {
        parser = mapper.getFactory().createParser(file);
    }

    @Override
    protected void after() throws IOException {
        parser.close();
    }

    @Override
    public boolean next() throws IOException {
        readNext();
        return next != null;
    }

    @Override
    public StarSystemData getSystem() {
        return next;
    }

    private void readNext() throws IOException {
        JsonToken token;
        next = null;
        while ((token = parser.nextToken()) != null){
            if (token == JsonToken.START_OBJECT){
                JsonNode node = parser.readValueAsTree();
                next = new EDDBSystemData(node, type);
                break;
            }
        }
    }

    public static EDDBParser createSystemParser(File file){
        return new EDDBParser(file, FILE_TYPE.SYSTEMS);
    }

    public enum FILE_TYPE {
        SYSTEMS, STATIONS;

        public boolean hasSystemData(){
            return this == SYSTEMS;
        }

        public boolean hasStationsData(){
            return this == STATIONS;
        }
    }

}

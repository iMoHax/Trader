package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.core.Market;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;

public class Store {
    private final static Logger LOG = LoggerFactory.getLogger(Store.class);
    private final static String XSD_FILE = "/store/trader.xsd";
    private static Schema schema;

    private static SAXParser getParser() throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        if (schema == null) initSchema();
        factory.setSchema(schema);
        return factory.newSAXParser();
    }

    public static Market loadFromFile(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        return loadFromFile(is, new MarketDocHandler());
    }

    public static Market loadFromFile(InputStream is, MarketDocHandler docHandler) throws ParserConfigurationException, SAXException, IOException {
        LOG.debug("Load market from stream");
        SAXParser parser = getParser();
        parser.parse(is, docHandler);
        return docHandler.getWorld();
    }

    public static Market loadFromFile(File xmlFile) throws IOException, SAXException, ParserConfigurationException {
        return loadFromFile(xmlFile, new MarketDocHandler());
    }

    public static Market loadFromFile(File xmlFile, MarketDocHandler docHandler) throws IOException, SAXException, ParserConfigurationException {
        LOG.debug("Load market from file {}", xmlFile);
        SAXParser parser = getParser();
        parser.parse(xmlFile, docHandler);
        return docHandler.getWorld();
    }

    public static void saveToFile(Market market, File xmlFile) throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        LOG.debug("Save market {} to file {}", market, xmlFile);
        MarketStreamWriter writer = new MarketStreamWriter(market);
        writer.save(xmlFile);
    }

    private static void initSchema() throws SAXException {
        Source xsdSource = new StreamSource(Store.class.getResourceAsStream(XSD_FILE));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = schemaFactory.newSchema(xsdSource);
    }

}

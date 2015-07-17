package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.analysis.FilteredMarket;
import ru.trader.core.Market;
import ru.trader.core.MarketAnalyzer;
import ru.trader.store.simple.SimpleMarket;
import ru.trader.store.simple.Store;
import ru.trader.store.XSSFImporter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class World {
    private static Market world;
    private static final String STORE_FILE="world.xml";
    private final static Logger LOG = LoggerFactory.getLogger(World.class);

    static {
        try {
            File file = new File(STORE_FILE);
            if (file.exists()) world = Store.loadFromFile(file);
                else world = new SimpleMarket();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save() throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        Store.saveToFile(world, new File("world.xml"));
        world.commit();
    }

    public static void saveTo(File file) throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        Store.saveToFile(world, file);
        world.commit();
    }

    public static void imp(File file) throws IOException, SAXException {
        LOG.info("Import from {}", file.getName());
        XSSFImporter xssfImporter = new XSSFImporter(file);
        world = xssfImporter.doImport();
    }

    public static void impXml(File file) throws ParserConfigurationException, SAXException, IOException {
        LOG.info("Import from {}", file.getName());
        Market market = Store.loadFromFile(file);
        world.add(market);
    }


    public static Market getMarket() {
        return world;
    }

    public static MarketAnalyzer buildAnalyzer(Market market){
        FilteredMarket fMarket = new FilteredMarket(market, Main.SETTINGS.getFilter(market));
        return new MarketAnalyzer(fMarket, Main.SETTINGS.getProfile());
    }
}

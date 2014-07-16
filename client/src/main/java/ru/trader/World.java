package ru.trader;

import org.xml.sax.SAXException;
import ru.trader.core.Market;
import ru.trader.core.SimpleMarket;
import ru.trader.model.ModelFabrica;
import ru.trader.store.Store;
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
        world.setChange(false);
    }

    public static void imp(File file) throws IOException, SAXException {
        XSSFImporter xssfImporter = new XSSFImporter(file);
        world = xssfImporter.doImport();
        ModelFabrica.clear();
        world.setChange(true);
    }

    public static Market getMarket() {
        return world;
    }

}

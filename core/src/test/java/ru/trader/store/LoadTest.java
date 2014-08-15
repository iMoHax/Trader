package ru.trader.store;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.core.Market;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class LoadTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(LoadTest.class);

    @Test
    public void testLoad(){
        LOG.info("Start world load test");
        InputStream is = getClass().getResourceAsStream("/test.xml");
        Market world;
        try {
            world = Store.loadFromFile(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(e);
        }
        assertNotNull(world);
    }

}

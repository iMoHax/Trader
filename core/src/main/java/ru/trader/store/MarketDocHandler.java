package ru.trader.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ru.trader.core.*;

import java.util.HashMap;

public class MarketDocHandler extends DefaultHandler {
    private final static Logger LOG = LoggerFactory.getLogger(MarketDocHandler.class);

    protected final static String MARKET = "market";
    protected final static String ITEM_LIST = "items";
    protected final static String ITEM = "item";
    protected final static String VENDOR_LIST = "vendors";
    protected final static String VENDOR = "vendor";
    protected final static String OFFER = "offer";

    protected final static String ID_ATTR = "id";
    protected final static String NAME_ATTR = "name";
    protected final static String TYPE_ATTR = "type";
    protected final static String PRICE_ATTR = "price";
    protected final static String ITEM_ATTR = "item";

    protected Market world;
    protected Vendor curVendor;
    protected final HashMap<String,Item> items = new HashMap<>();

    @Override
    public void startDocument() throws SAXException {
        world = new SimpleMarket();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName){
            case ITEM: parseItem(attributes);
                break;
            case VENDOR: parseVendor(attributes);
                break;
            case OFFER: parseOffer(attributes);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName){
            case VENDOR: world.add(curVendor);
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        world.setChange(false);
    }

    protected void parseVendor(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        onVendor(name);
    }

    protected void parseItem(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        String id = attributes.getValue(ID_ATTR);
        onItem(name, id);
    }

    protected void parseOffer(Attributes attributes) throws SAXException {
        String refid = attributes.getValue(ITEM_ATTR);
        Item item = items.get(refid);
        if (item == null)
            throw new SAXException(String.format("Item (id = %s) not found", refid));
        OFFER_TYPE offerType = OFFER_TYPE.valueOf(attributes.getValue(TYPE_ATTR));
        double price = Double.valueOf(attributes.getValue(PRICE_ATTR));
        onOffer(offerType, item, price);
    }

    protected void onOffer(OFFER_TYPE offerType, Item item, double price){
        Offer offer = new Offer(offerType, item, price);
        curVendor.add(offer);
    }

    protected void onVendor(String name){
        curVendor = new SimpleVendor(name);
    }

    protected void onItem(String name, String id) {
        Item item = new Item(name);
        world.add(item);
        items.put(id, item);
    }

    public Market getWorld(){
        return world;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        LOG.warn("warning on parse file",e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        LOG.warn("Error on parse file",e);
        throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        LOG.warn("Fatal error on parse file",e);
        throw e;
    }

}

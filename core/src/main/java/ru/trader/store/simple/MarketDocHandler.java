package ru.trader.store.simple;

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
    protected final static String GROUP = "group";

    protected final static String ID_ATTR = "id";
    protected final static String NAME_ATTR = "name";
    protected final static String TYPE_ATTR = "type";
    protected final static String PRICE_ATTR = "price";
    protected final static String ITEM_ATTR = "item";
    protected final static String X_ATTR = "x";
    protected final static String Y_ATTR = "y";
    protected final static String Z_ATTR = "z";

    protected Market world;
    protected Vendor curVendor;
    protected Group curGroup;
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
            case GROUP: parseGroup(attributes);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName){
            case VENDOR: world.add(curVendor);
                break;
            case GROUP: curGroup = null;
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        world.setChange(false);
    }

    protected void parseVendor(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        String x = attributes.getValue(X_ATTR);
        String y = attributes.getValue(Y_ATTR);
        String z = attributes.getValue(Z_ATTR);
        LOG.debug("parse vendor {} position ({};{};{})", name, x, y, z);
        onVendor(name, x != null ? Double.valueOf(x) : 0, y != null ? Double.valueOf(y) : 0, z != null ? Double.valueOf(z) : 0);
    }

    protected void parseItem(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        String id = attributes.getValue(ID_ATTR);
        LOG.debug("parse item {} ({})", name, id);
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

    protected void parseGroup(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        GROUP_TYPE type = GROUP_TYPE.valueOf(attributes.getValue(TYPE_ATTR));
        LOG.debug("parse group {} ({})", name, type);
        onGroup(name, type);
    }

    protected void onOffer(OFFER_TYPE offerType, Item item, double price){
        Offer offer = new SimpleOffer(offerType, item, price);
        curVendor.add(offer);
    }

    protected void onVendor(String name, double x, double y, double z){
        curVendor = new SimpleVendor(name);
        curVendor.setX(x);
        curVendor.setY(y);
        curVendor.setZ(z);
    }

    protected void onItem(String name, String id) {
        Item item = new SimpleItem(name);
        item.setGroup(curGroup);
        world.add(item);
        items.put(id, item);
    }

    protected void onGroup(String name, GROUP_TYPE type) {
        curGroup = new SimpleGroup(name, type);
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

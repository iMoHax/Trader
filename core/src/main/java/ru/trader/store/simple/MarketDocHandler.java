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
    protected final static String PLACES_LIST = "places";
    protected final static String PLACE = "place";
    protected final static String VENDOR = "vendor";
    protected final static String SERVICES_LIST = "services";
    protected final static String SERVICE = "service";
    protected final static String OFFER = "offer";
    protected final static String GROUP = "group";

    protected final static String ID_ATTR = "id";
    protected final static String NAME_ATTR = "name";
    protected final static String TYPE_ATTR = "type";
    protected final static String PRICE_ATTR = "price";
    protected final static String COUNT_ATTR = "count";
    protected final static String ITEM_ATTR = "item";
    protected final static String DISTANCE_ATTR = "distance";
    protected final static String X_ATTR = "x";
    protected final static String Y_ATTR = "y";
    protected final static String Z_ATTR = "z";

    protected SimpleMarket world;
    protected Vendor curVendor;
    protected Place curPlace;
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
            case PLACE: parsePlace(attributes);
                break;
            case VENDOR: parseVendor(attributes);
                break;
            case SERVICE: parseService(attributes);
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
            case PLACE: curPlace = null;
                break;
            case VENDOR: curVendor = null;
                break;
            case GROUP: curGroup = null;
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        world.commit();
    }

    protected void parsePlace(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        String x = attributes.getValue(X_ATTR);
        String y = attributes.getValue(Y_ATTR);
        String z = attributes.getValue(Z_ATTR);
        LOG.debug("parse place {} position ({};{};{})", name, x, y, z);
        onPlace(name, x != null ? Double.valueOf(x) : 0, y != null ? Double.valueOf(y) : 0, z != null ? Double.valueOf(z) : 0);
    }

    protected void parseVendor(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        String distance = attributes.getValue(DISTANCE_ATTR);
        LOG.debug("parse vendor {}, distance {}", name, distance);
        onVendor(name, distance != null ? Double.valueOf(distance) : 0);
    }

    protected void parseService(Attributes attributes) throws SAXException {
        SERVICE_TYPE type = SERVICE_TYPE.valueOf(attributes.getValue(TYPE_ATTR));
        LOG.debug("add service {}", type);
        onService(type);
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
        long count = Long.valueOf(attributes.getValue(COUNT_ATTR));
        onOffer(offerType, item, price, count);
    }

    protected void parseGroup(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        GROUP_TYPE type = GROUP_TYPE.valueOf(attributes.getValue(TYPE_ATTR));
        LOG.debug("parse group {} ({})", name, type);
        onGroup(name, type);
    }

    protected void onOffer(OFFER_TYPE offerType, Item item, double price, long count){
        curVendor.addOffer(offerType, item, price, count);
    }

    protected void onPlace(String name, double x, double y, double z){
        curPlace = world.addPlace(name, x, y, z);
    }

    protected void onVendor(String name, double distance){
        curVendor = curPlace.addVendor(name);
        curVendor.setDistance(distance);
    }

    protected void onService(SERVICE_TYPE type){
        curVendor.add(type);
    }

    protected void onItem(String name, String id) {
        Item item = world.addItem(name, curGroup);
        items.put(id, item);
    }

    protected void onGroup(String name, GROUP_TYPE type) {
        curGroup = world.addGroup(name, type);
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

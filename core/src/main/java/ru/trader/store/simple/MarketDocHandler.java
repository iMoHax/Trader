package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ru.trader.core.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumSet;
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
    protected final static String ILLEGAL_FACTION_ATTR = "illegalf";
    protected final static String ILLEGAL_GOVERNMENT__ATTR = "illegalg";
    protected final static String LEGAL_FACTION_ATTR = "legalf";
    protected final static String LEGAL_GOVERNMENT__ATTR = "legalg";
    protected final static String DISTANCE_ATTR = "distance";
    protected final static String X_ATTR = "x";
    protected final static String Y_ATTR = "y";
    protected final static String Z_ATTR = "z";
    protected final static String FACTION_ATTR = "faction";
    protected final static String GOVERNMENT_ATTR = "government";
    protected final static String POWER_ATTR = "power";
    protected final static String POWER_STATE_ATTR = "control";
    protected final static String ECONOMIC_ATTR = "economic1";
    protected final static String SUB_ECONOMIC_ATTR = "economic2";
    protected final static String MODIFIED_ATTR = "modified";

    protected SimpleMarket world;
    protected Vendor curVendor;
    protected Place curPlace;
    protected Group curGroup;
    protected LocalDateTime modified;
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
            case VENDOR:
                curVendor.setModifiedTime(modified);
                curVendor = null;
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
        String faction = attributes.getValue(FACTION_ATTR);
        String government = attributes.getValue(GOVERNMENT_ATTR);
        LOG.debug("parse place {} position ({};{};{}), faction {}, government {}", name, x, y, z, faction, government);
        onPlace(name, x != null ? Double.valueOf(x) : 0, y != null ? Double.valueOf(y) : 0, z != null ? Double.valueOf(z) : 0,
                faction != null ? FACTION.valueOf(faction) : null, government != null ? GOVERNMENT.valueOf(government) : null
                );
        String power = attributes.getValue(POWER_ATTR);
        String powerState = attributes.getValue(POWER_STATE_ATTR);
        if (powerState != null && power != null){
            updatePlace(POWER.valueOf(power), POWER_STATE.valueOf(powerState));
        }
    }

    protected void parseVendor(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        String type = attributes.getValue(TYPE_ATTR);
        String distance = attributes.getValue(DISTANCE_ATTR);
        String faction = attributes.getValue(FACTION_ATTR);
        String government = attributes.getValue(GOVERNMENT_ATTR);
        String economic = attributes.getValue(ECONOMIC_ATTR);
        String subEconomic = attributes.getValue(SUB_ECONOMIC_ATTR);
        String modifiedTime = attributes.getValue(MODIFIED_ATTR);
        modified = modifiedTime != null ? LocalDateTime.parse(modifiedTime) : null;
        LOG.debug("parse vendor {}, type {}, distance {}, faction {}, government {}", name, type, distance, faction, government);
        onVendor(name, type != null ? STATION_TYPE.valueOf(type) : null, distance != null ? Double.valueOf(distance) : 0,
                faction != null ? FACTION.valueOf(faction) : null, government != null ? GOVERNMENT.valueOf(government) : null);
        updateVendor(economic != null ? ECONOMIC_TYPE.valueOf(economic) : null, subEconomic != null ? ECONOMIC_TYPE.valueOf(subEconomic) : null);
    }

    protected void parseService(Attributes attributes) throws SAXException {
        SERVICE_TYPE type = SERVICE_TYPE.valueOf(attributes.getValue(TYPE_ATTR));
        LOG.debug("add service {}", type);
        onService(type);
    }


    protected void parseItem(Attributes attributes) throws SAXException {
        String name = attributes.getValue(NAME_ATTR);
        String id = attributes.getValue(ID_ATTR);
        String illegalFactions = attributes.getValue(ILLEGAL_FACTION_ATTR);
        String illegalGovernments = attributes.getValue(ILLEGAL_GOVERNMENT__ATTR);
        EnumSet<FACTION> factions = EnumSet.noneOf(FACTION.class);
        if (illegalFactions != null){
            for (String f : illegalFactions.split(",")) {
                factions.add(FACTION.valueOf(f));
            }
        }
        EnumSet<GOVERNMENT> governments = EnumSet.noneOf(GOVERNMENT.class);
        if (illegalGovernments != null){
            for (String f : illegalGovernments.split(",")) {
                governments.add(GOVERNMENT.valueOf(f));
            }
        }

        String legalFactions = attributes.getValue(LEGAL_FACTION_ATTR);
        String legalGovernments = attributes.getValue(LEGAL_GOVERNMENT__ATTR);
        EnumSet<FACTION> legalf = EnumSet.noneOf(FACTION.class);
        if (legalFactions != null){
            for (String f : legalFactions.split(",")) {
                legalf.add(FACTION.valueOf(f));
            }
        }
        EnumSet<GOVERNMENT> legalg = EnumSet.noneOf(GOVERNMENT.class);
        if (legalGovernments != null){
            for (String f : legalGovernments.split(",")) {
                legalg.add(GOVERNMENT.valueOf(f));
            }
        }

        LOG.debug("parse item {} ({}), illegal - {}, {}, legal - {}, {}", name, id, factions, governments, legalf, legalg);
        onItem(name, id, factions, governments, legalf, legalg);
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

    protected void onPlace(String name, double x, double y, double z, FACTION faction, GOVERNMENT government){
        curPlace = world.addPlace(name, x, y, z);
        curPlace.setFaction(faction);
        curPlace.setGovernment(government);
    }

    protected void updatePlace(POWER power, POWER_STATE powerState){
        curPlace.setPower(power, powerState);
    }

    protected void onVendor(String name, STATION_TYPE type, double distance, FACTION faction, GOVERNMENT government){
        curVendor = curPlace.addVendor(name);
        curVendor.setType(type);
        curVendor.setDistance(distance);
        curVendor.setFaction(faction);
        curVendor.setGovernment(government);
    }

    protected void updateVendor(ECONOMIC_TYPE economic, ECONOMIC_TYPE subEconomic){
        curVendor.setEconomic(economic);
        curVendor.setSubEconomic(subEconomic);
        curVendor.setModifiedTime(modified);
    }

    protected void onService(SERVICE_TYPE type){
        curVendor.add(type);
    }

    protected void onItem(String name, String id, Collection<FACTION> illegalFactions, Collection<GOVERNMENT> illegalGovernment,
                          Collection<FACTION> legalFactions, Collection<GOVERNMENT> legalGovernment) {
        Item item = world.addItem(name, curGroup);
        for (FACTION faction : illegalFactions) {
            item.setIllegal(faction, true);
        }
        for (GOVERNMENT government : illegalGovernment) {
            item.setIllegal(government, true);
        }
        for (FACTION faction : legalFactions) {
            item.setLegal(faction, true);
        }
        for (GOVERNMENT government : legalGovernment) {
            item.setLegal(government, true);
        }
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

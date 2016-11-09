package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MarketStreamWriter {
    private final static Logger LOG = LoggerFactory.getLogger(MarketStreamWriter.class);

    private final Map<Item, String> items;
    private final Market market;
    private XMLStreamWriter out;

    public MarketStreamWriter(Market market) {
        this.market = market;
        this.items = generateId(market.getItems());
    }

    public void save(File xmlFile) throws XMLStreamException, UnsupportedEncodingException, FileNotFoundException {
        OutputStreamWriter outputStream = null;
        out = null;
        try {
            outputStream = new OutputStreamWriter(new FileOutputStream(xmlFile), "utf-8");
            out = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            out.writeStartDocument();
            writeMarket();
            out.writeEndDocument();
        } finally {
            if (out!=null) try {out.close();} catch (XMLStreamException e) {LOG.warn("Error on close:",e);}
            if (outputStream!=null) try {outputStream.close();} catch (IOException e){LOG.warn("Error on close:",e);}
        }
    }

    protected void writeMarket() throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.MARKET);
        writeItems();
        writePlaces();
        out.writeEndElement();
    }

    protected void writeItems() throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.ITEM_LIST);
        Group group = null;
        List<Item> sortedItems = new ArrayList<>(market.getItems());
        sortedItems.sort(itemComparator);
        for (Item entry : sortedItems) {
            if (group!=entry.getGroup()){
                if (group != null) out.writeEndElement();
                group = entry.getGroup();
                if (group != null) writeGroup(group);
            }
            writeItem(entry, items.get(entry));
        }
        if (group != null) out.writeEndElement();

        out.writeEndElement();
    }

    protected void writeItem(Item item, String id) throws XMLStreamException {
        out.writeEmptyElement(MarketDocHandler.ITEM);
        out.writeAttribute(MarketDocHandler.NAME_ATTR, item.getName());
        out.writeAttribute(MarketDocHandler.ID_ATTR, id);
        Collection<FACTION> factions = item.getIllegalFactions();
        if (!factions.isEmpty()) {
            String str = factions.stream().map(FACTION::toString).collect(Collectors.joining(","));
            out.writeAttribute(MarketDocHandler.ILLEGAL_FACTION_ATTR, str);
        }
        Collection<GOVERNMENT> governments = item.getIllegalGovernments();
        if (!governments.isEmpty()) {
            String str = governments.stream().map(GOVERNMENT::toString).collect(Collectors.joining(","));
            out.writeAttribute(MarketDocHandler.ILLEGAL_GOVERNMENT__ATTR, str);
        }
        factions = item.getLegalFactions();
        if (!factions.isEmpty()) {
            String str = factions.stream().map(FACTION::toString).collect(Collectors.joining(","));
            out.writeAttribute(MarketDocHandler.LEGAL_FACTION_ATTR, str);
        }
        governments = item.getLegalGovernments();
        if (!governments.isEmpty()) {
            String str = governments.stream().map(GOVERNMENT::toString).collect(Collectors.joining(","));
            out.writeAttribute(MarketDocHandler.LEGAL_GOVERNMENT__ATTR, str);
        }

    }

    protected void writePlaces() throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.PLACES_LIST);
        for (Place place : market.get()) {
            writePlace(place);
        }
        out.writeEndElement();
    }

    protected void writePlace(Place place) throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.PLACE);
        out.writeAttribute(MarketDocHandler.NAME_ATTR, place.getName());
        if (place.getFaction() != null) {
            out.writeAttribute(MarketDocHandler.FACTION_ATTR, String.valueOf(place.getFaction()));
        }
        if (place.getGovernment() != null) {
            out.writeAttribute(MarketDocHandler.GOVERNMENT_ATTR, String.valueOf(place.getGovernment()));
        }
        if (place.getPower() != null){
            out.writeAttribute(MarketDocHandler.POWER_ATTR, String.valueOf(place.getPower()));
        }
        if (place.getPowerState() != null){
            out.writeAttribute(MarketDocHandler.POWER_STATE_ATTR, String.valueOf(place.getPowerState()));
        }
        out.writeAttribute(MarketDocHandler.X_ATTR, String.valueOf(place.getX()));
        out.writeAttribute(MarketDocHandler.Y_ATTR, String.valueOf(place.getY()));
        out.writeAttribute(MarketDocHandler.Z_ATTR, String.valueOf(place.getZ()));

        out.writeAttribute(MarketDocHandler.POPULATION_ATTR, String.valueOf(place.getPopulation()));
        out.writeAttribute(MarketDocHandler.UPKEEP_ATTR, String.valueOf(place.getUpkeep()));
        out.writeAttribute(MarketDocHandler.INCOME_ATTR, String.valueOf(place.getIncome()));

        for (Vendor vendor : place.get()) {
            writeVendor(vendor);
        }
        out.writeEndElement();
    }

    protected void writeVendor(Vendor vendor) throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.VENDOR);
        out.writeAttribute(MarketDocHandler.NAME_ATTR, vendor.getName());
        if (vendor.getType() != null){
            out.writeAttribute(MarketDocHandler.TYPE_ATTR, String.valueOf(vendor.getType()));
        }
        if (vendor.getFaction() != null) {
            out.writeAttribute(MarketDocHandler.FACTION_ATTR, String.valueOf(vendor.getFaction()));
        }
        if (vendor.getGovernment() != null) {
            out.writeAttribute(MarketDocHandler.GOVERNMENT_ATTR, String.valueOf(vendor.getGovernment()));
        }
        if (vendor.getEconomic() != null){
            out.writeAttribute(MarketDocHandler.ECONOMIC_ATTR, String.valueOf(vendor.getEconomic()));
        }
        if (vendor.getSubEconomic() != null){
            out.writeAttribute(MarketDocHandler.SUB_ECONOMIC_ATTR, String.valueOf(vendor.getSubEconomic()));
        }
        out.writeAttribute(MarketDocHandler.DISTANCE_ATTR, String.valueOf(vendor.getDistance()));
        if (vendor.getModifiedTime() != null){
            out.writeAttribute(MarketDocHandler.MODIFIED_ATTR, vendor.getModifiedTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        out.writeStartElement(MarketDocHandler.SERVICES_LIST);
        for (SERVICE_TYPE service_type : vendor.getServices()) {
            out.writeEmptyElement(MarketDocHandler.SERVICE);
            out.writeAttribute(MarketDocHandler.TYPE_ATTR, service_type.toString());
        }
        out.writeEndElement();
        for (Offer offer : vendor.getAllSellOffers()) {
            writeOffer(offer);
        }
        for (Offer offer : vendor.getAllBuyOffers()) {
            writeOffer(offer);
        }
        out.writeEndElement();
    }

    protected void writeOffer(Offer offer) throws XMLStreamException {
        out.writeEmptyElement(MarketDocHandler.OFFER);
        out.writeAttribute(MarketDocHandler.TYPE_ATTR, offer.getType().toString());
        out.writeAttribute(MarketDocHandler.ITEM_ATTR, items.get(offer.getItem()));
        out.writeAttribute(MarketDocHandler.PRICE_ATTR, String.valueOf(offer.getPrice()));
        out.writeAttribute(MarketDocHandler.COUNT_ATTR, String.valueOf(offer.getCount()));
    }

    protected void writeGroup(Group group) throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.GROUP);
        out.writeAttribute(MarketDocHandler.NAME_ATTR, group.getName());
        out.writeAttribute(MarketDocHandler.TYPE_ATTR, group.getType().toString());
    }

    private static Map<Item, String> generateId(Collection<Item> items){
        HashMap<Item, String> res = new HashMap<>(items.size());
        int index=0;
        for (Item item : items) {
            res.put(item, "i"+index);
            index++;
        }
        return res;
    }

    private final static Comparator<Item> itemComparator = (i1, i2) -> {
        Group g1 = i1.getGroup();
        Group g2 = i2.getGroup();
        if (g1 == null) return -1;
        if (g2 == null) return 1;
        int cmp = g1.getType().compareTo(g2.getType());
        if (cmp != 0) return cmp;
        cmp = g1.getName().compareTo(g2.getName());
        if (cmp != 0) return cmp;
        return i1.getName().compareTo(i2.getName());
    };
}

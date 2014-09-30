package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        writeVendors(market.get());
        out.writeEndElement();
    }

    protected void writeItems() throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.ITEM_LIST);
        Group group = null;
        for (Item entry : market.getItems()) {
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
    }

    protected void writeVendors(Collection<Vendor> vendors) throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.VENDOR_LIST);
        for (Vendor vendor : vendors) {
            writeVendor(vendor);
        }
        out.writeEndElement();
    }

    protected void writeVendor(Vendor vendor) throws XMLStreamException {
        out.writeStartElement(MarketDocHandler.VENDOR);
        out.writeAttribute(MarketDocHandler.NAME_ATTR, vendor.getName());
        out.writeAttribute(MarketDocHandler.X_ATTR, String.valueOf(vendor.getX()));
        out.writeAttribute(MarketDocHandler.Y_ATTR, String.valueOf(vendor.getY()));
        out.writeAttribute(MarketDocHandler.Z_ATTR, String.valueOf(vendor.getZ()));

        for (Offer offer : vendor.getAllOffers()) {
            writeOffer(offer);
        }
        out.writeEndElement();
    }

    protected void writeOffer(Offer offer) throws XMLStreamException {
        out.writeEmptyElement(MarketDocHandler.OFFER);
        out.writeAttribute(MarketDocHandler.TYPE_ATTR, offer.getType().toString());
        out.writeAttribute(MarketDocHandler.ITEM_ATTR, items.get(offer.getItem()));
        out.writeAttribute(MarketDocHandler.PRICE_ATTR, String.valueOf(offer.getPrice()));
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
}

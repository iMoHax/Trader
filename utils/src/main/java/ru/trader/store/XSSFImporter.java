package ru.trader.store;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import ru.trader.core.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XSSFImporter {
    private final static Logger LOG = LoggerFactory.getLogger(XSSFImporter.class);

    private OPCPackage pkg;
    private final XSSFReader reader;
    protected final Iterator<InputStream> sheetItr;
    protected final Market market;
    protected final List<Vendor> vendors;

    public XSSFImporter(File file) throws IOException {
        pkg = null;
        try {
            pkg = OPCPackage.open(file.getPath());
            reader = new XSSFReader(pkg);
            sheetItr = reader.getSheetsData();
            market = new SimpleMarket();
            vendors = new ArrayList<>();
        } catch (OpenXML4JException e) {
            throw new IOException(e);
        }
    }

    public Market doImport() throws IOException, SAXException {
        if (pkg==null) throw new IllegalStateException("No init package");
        try {
            XMLReader saxParser = XMLReaderFactory.createXMLReader();
            saxParser.setContentHandler(getHandler());
            saxParser.parse(getInputSource());
            market.addVendors(vendors);
        } finally {
            if (pkg!=null) {
                try { pkg.close();} catch (IOException e) {LOG.warn("Error on close pkg",e);}
                pkg = null;
            }

        }
        return market;
    }


    protected InputSource getInputSource() {
        return new InputSource(sheetItr.next());
    }

    protected ContentHandler getHandler() throws IOException, SAXException {
        StylesTable styles;
        try {
            styles = reader.getStylesTable();
        } catch (InvalidFormatException e) {
            throw new SAXException(e);
        }
        ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(pkg);
        return new XSSFSheetXMLHandler(styles, sharedStrings, new LoaderSheetContentsHandler(), false);
    }


    protected class LoaderSheetContentsHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private Item item;

        @Override
        public void startRow(int rowNum) {
        }

        @Override
        public void endRow() {
        }

        @Override
        public void cell(String cellReference, String formattedValue) {
            LOG.trace("Parce cell {}, value = {}", cellReference, formattedValue);
            if (formattedValue==null||formattedValue.isEmpty()) return;
            RC cell = new RC(cellReference);
            if (cell.row == 1) {
                if (cell.column > 1 &&cell.column % 2 == 0){
                    LOG.trace("add vendor");
                    vendors.add(new SimpleVendor(formattedValue));
                }
            } else if (cell.row>2){
                if (cell.column == 1){
                    LOG.trace("create item");
                    item = new Item(formattedValue);
                    market.add(item);
                } else {
                    LOG.trace("add offer");
                    Offer offer = new Offer(cell.column % 2 == 0? OFFER_TYPE.BUY : OFFER_TYPE.SELL, item, Double.valueOf(formattedValue));
                    vendors.get(cell.column/2 -1).add(offer);
                }
            }
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
        }

    }


    private static final Pattern CELL_REF = Pattern.compile("(\\D+)(\\d+)");
    private class RC {
        int row;
        int column;

        public RC(String cellReference){
            Matcher matcher = CELL_REF.matcher(cellReference);
            if (matcher.find()){
                column = wordToColumn(matcher.group(1));
                row = Integer.valueOf(matcher.group(2));
            }
        }
    }

    public static int wordToColumn(String word){
        word = word.toUpperCase();
        int column = -1;
        for (int i = 0; i < word.length(); ++i) {
            int c = word.charAt(i);
            column = (column + 1) * 26 + c - 'A';
        }
        return column+1;
    }

}

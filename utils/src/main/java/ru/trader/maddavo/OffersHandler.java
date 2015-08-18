package ru.trader.maddavo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OffersHandler implements ParseHandler {
    private final static Logger LOG = LoggerFactory.getLogger(OffersHandler.class);

    private final Map<String, Item> items;
    private final Map<String, Vendor> stations;
    private final boolean withRemove;
    private Vendor station;


    private final Map<Item, OfferData> sellUpdates = new HashMap<>(100, 0.9f);
    private final Map<Item, OfferData> buyUpdates = new HashMap<>(100, 0.9f);

    protected OffersHandler(Market market, boolean withRemove) {
        this.withRemove = withRemove;
        items = market.getItems().stream().collect(Collectors.toMap(Item::getName, i -> i));
        stations = market.getVendors().stream().collect(Collectors.toMap(this::getStationId, s -> s));
    }

    private String getStationId(Vendor vendor){
        return vendor.getPlace().getName()+"/"+vendor.getName();
    }

    @Override
    public void parse(String str) throws IOException {
        if (str.isEmpty()) {
            if (station != null){
                updateStation();
            }
            return;
        }
        if (str.startsWith("#")) return;
        if (str.startsWith("@")){
            if (station != null){
                updateStation();
            }
            parseStation(str);
        } else {
            if (station == null){
                LOG.trace("Station not exists, skip");
                return;
            }
            parseLine(str);
        }
    }

    private void updateStation() {
        LOG.debug("Update offers of station {}", station);
        station.getAllBuyOffers().forEach(this::updateOffer);
        station.getAllSellOffers().forEach(this::updateOffer);
        buyUpdates.entrySet().forEach( entry -> addOffer(entry, OFFER_TYPE.BUY));
        sellUpdates.entrySet().forEach( entry -> addOffer(entry, OFFER_TYPE.SELL));
        buyUpdates.clear();
        sellUpdates.clear();
        station = null;
    }

    private void updateOffer(Offer offer) {
        Map<Item, OfferData> offerDatas = offer.getType() == OFFER_TYPE.SELL ? sellUpdates : buyUpdates;
        OfferData data = offerDatas.get(offer.getItem());
        if (data != null){
            if (data.price != offer.getPrice()) offer.setPrice(data.price);
            if (data.count != null && data.count != offer.getCount()) offer.setCount(data.count);
            data.isnew = false;
        } else {
            if (withRemove && offer.getItem().getGroup().isMarket()){
                station.remove(offer);
            }
        }
    }

    private void addOffer(Map.Entry<Item,OfferData> entry, OFFER_TYPE type){
        OfferData offer = entry.getValue();
        if (offer.isnew){
            station.addOffer(type, entry.getKey(), offer.price, offer.count != null ? offer.count : -1);
        }
    }

    private void parseStation(String str) {
        StringBuilder sb = new StringBuilder(20);
        String system = "";
        String name;
        LOG.trace("Parse system line: {}", str);
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '#') break;
            if (c == ' '){
                //trim
                if (sb.length() == 0 || i == str.length()-1) continue;
                char next = str.charAt(i+1);
                if (next == ' ' || next == '/') continue;
            }
            if (c == '/'){
                system = sb.toString();
                sb = new StringBuilder(20);
            } else {
                sb.append(c);
            }
        }

        name = sb.toString();
        LOG.trace("system: {}, station: {}", system, name);
        String id = system + "/" +name;
        station = stations.get(id);
        if (station == null){
            LOG.warn("Station {} not found", id);
        }
    }

    private final static String NAME_REGEXP = "(\\S.+\\S)";
    private final static String BUY_SELL_REGEXP = "([\\d]+|[\\?-])";
    private final static String SUPPLY_DEMAND_REGEXP = "([\\d]+|[\\?-])([LMH\\?])?";
    private final static String DATE_REGEXP = "(\\d+(?:[- :]+\\d+)+)";
    private final static Pattern PRICE_REGEXP = Pattern.compile("\\s*" + NAME_REGEXP + "\\s+" + BUY_SELL_REGEXP + "\\s+" + BUY_SELL_REGEXP + "\\s+"+ SUPPLY_DEMAND_REGEXP + "\\s+"+ SUPPLY_DEMAND_REGEXP + "\\s+"+ DATE_REGEXP +"\\s*(:?#.+)?");


    private void parseLine(String str){
        Matcher matcher = PRICE_REGEXP.matcher(str);
        if (matcher.find()){
            String name = matcher.group(1);
            Double buy = getDoubleValue(matcher.group(2));
            Double sell = getDoubleValue(matcher.group(3));
            Long demand = getLongValue(matcher.group(4));
            Long supply = getLongValue(matcher.group(6));
            Item item = items.get(ItemConverter.getItemId(name));
            if (item != null){
                if (buy != null && buy > 0){
                    buyUpdates.put(item, new OfferData(buy, demand));
                }
                if (sell != null && sell > 0){
                    sellUpdates.put(item, new OfferData(sell, supply));
                }
            } else {
                LOG.warn("Item {} not found, line: {}", name, str);
            }

        } else {
            LOG.trace("Line is not prices: {}", str);
        }

    }

    private Double getDoubleValue(String string){
        if ("?".equals(string)) return null;
        if ("-".equals(string)) return 0.0;
        return Double.valueOf(string);
    }

    private Long getLongValue(String string){
        if ("?".equals(string)) return null;
        if ("-".equals(string)) return 0L;
        return Long.valueOf(string);
    }

    private class OfferData {
        private Double price;
        private Long count;
        private boolean isnew;

        private OfferData(Double price, Long count) {
            this.price = price;
            this.count = count;
            isnew = true;
        }
    }
}

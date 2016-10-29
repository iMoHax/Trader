package ru.trader.emdn.entities;

import com.fasterxml.jackson.databind.JsonNode;
import ru.trader.store.imp.ImportDataError;
import ru.trader.store.imp.entities.ItemDataBase;

public class EDDNItemData extends ItemDataBase {
    private final JsonNode node;
    private final SUPPORT_VERSIONS version;

    public EDDNItemData(JsonNode node, SUPPORT_VERSIONS version) {
        this.node = node;
        this.version = version;
    }

    @Override
    public Long getId() {
        JsonNode n = node.get("id");
        if (n != null && n.isNumber()){
            return n.asLong();
        }
        return null;
    }

    @Override
    public String getName() {
        String name = null;
        JsonNode n;
        switch (version) {
            case V1:
                n = node.get("itemName");
                if (n != null) name = n.asText();
                break;
            default:
                n = node.get("name");
                if (n != null) name = n.asText();
                break;
        }
        if (name == null){
            throw new ImportDataError("EDDN message don't have commodity name");
        }
        return name;
    }

    @Override
    public long getBuyOfferPrice() {
        // buy offer price in trader = sell price in EDCE
        JsonNode n = node.get("sellPrice");
        if (n != null && n.isNumber()){
            return n.asLong();
        } else {
            throw new ImportDataError("EDDN message don't have commodity sell price");
        }
    }

    @Override
    public long getSellOfferPrice() {
        // sell offer price in trader = buy price in EDCE
        JsonNode n = node.get("buyPrice");
        if (n != null && n.isNumber()){
            return n.asLong();
        } else {
            throw new ImportDataError("EDDN message don't have commodity buy price");
        }
    }

    @Override
    public long getSupply() {
        JsonNode n;
        JsonNode ln;
        switch (version) {
            case V1:
                n = node.get("stationStock");
                ln = node.get("supplyLevel");
                break;
            case V2:
                n = node.get("supply");
                ln = node.get("supplyLevel");
                break;
            default:
                n = node.get("stock");
                ln = node.get("stockBracket");
                break;
        }
        if (n != null && n.isNumber()){
            if (ln != null && (ln.isNumber() && ln.asInt() == 0 || ln.asText().isEmpty())){
                return 0;
            }
            return n.asLong();
        } else {
            throw new ImportDataError("EDDN message don't have commodity supply");
        }
    }

    @Override
    public long getDemand() {
        JsonNode n = node.get("demand");
        JsonNode ln;
        switch (version) {
            case V1:
            case V2:
                ln = node.get("demandLevel");
                break;
            default:
                ln = node.get("demandBracket");
                break;
        }
        if (n != null && n.isNumber()){
            if (ln != null && (ln.isNumber() && ln.asInt() == 0 || ln.asText().isEmpty())){
                return 0;
            }
            return n.asLong();
        } else {
            throw new ImportDataError("EDDN message don't have commodity demand");
        }
    }
}

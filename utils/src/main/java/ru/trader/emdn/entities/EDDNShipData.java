package ru.trader.emdn.entities;

import com.fasterxml.jackson.databind.JsonNode;
import ru.trader.store.imp.ImportDataError;
import ru.trader.store.imp.entities.ShipDataBase;

public class EDDNShipData extends ShipDataBase {
    private final JsonNode node;
    private final SUPPORT_VERSIONS version;

    public EDDNShipData(JsonNode node, SUPPORT_VERSIONS version) {
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
        if (node != null && node.isTextual()){
            return node.asText();
        } else {
            throw new ImportDataError("EDDN message don't have ship name");
        }
    }

    @Override
    public Long getPrice() {
        return null;
    }
}

package ru.trader.emdn.entities;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;
import ru.trader.store.imp.ImportDataError;
import ru.trader.store.imp.entities.ItemData;
import ru.trader.store.imp.entities.ShipData;
import ru.trader.store.imp.entities.StationDataBase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class EDDNStationData extends StationDataBase {
    private final JsonNode node;
    private final SUPPORT_VERSIONS version;


    public EDDNStationData(JsonNode node, SUPPORT_VERSIONS version) {
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
        JsonNode n = node.get("stationName");
        if (n != null) name = n.asText();
        if (name == null){
            throw new ImportDataError("EDDN message don't have station name");
        }
        return name;
    }

    @Nullable
    @Override
    public Collection<ItemData> getCommodities() {
        JsonNode n;
        switch (version) {
            case V1_SHIPYARD:
            case V2_SHIPYARD:
                return null;
            case V1:
                Collection<ItemData> items = new ArrayList<>();
                items.add(new EDDNItemData(node, version));
                return items;
            default:
                n = node.get("commodities");
                if (n != null && n.isArray()){
                    items = new ArrayList<>();
                    for (Iterator<JsonNode> iterator = n.elements(); iterator.hasNext(); ) {
                        JsonNode itemNode = iterator.next();
                        items.add(new EDDNItemData(itemNode, version));
                    }
                    return items;
                } else {
                    throw new ImportDataError("EDDN message don't have commodities field");
                }
        }
    }

    @Nullable
    @Override
    public Collection<ShipData> getShips() {
        JsonNode n;
        switch (version) {
            case V1_SHIPYARD:
            case V2_SHIPYARD:
                n = node.get("ships");
                if (n != null && n.isArray()){
                    Collection<ShipData> ships = new ArrayList<>();
                    for (Iterator<JsonNode> iterator = n.elements(); iterator.hasNext(); ) {
                        JsonNode shipNode = iterator.next();
                        ships.add(new EDDNShipData(shipNode, version));
                    }
                    return ships;
                } else {
                    throw new ImportDataError("EDDN message don't have ships field");
                }
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public LocalDateTime getModifiedTime() {
        JsonNode n = node.get("timestamp");
        if (n != null && n.isTextual()){
            return LocalDateTime.parse(n.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else {
            throw new ImportDataError("EDDN message don't have timestamp field");
        }
    }
}

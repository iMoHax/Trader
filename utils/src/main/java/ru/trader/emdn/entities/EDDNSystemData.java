package ru.trader.emdn.entities;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;
import ru.trader.store.imp.ImportDataError;
import ru.trader.store.imp.entities.StarSystemDataBase;
import ru.trader.store.imp.entities.StationData;

import java.util.ArrayList;
import java.util.Collection;

public class EDDNSystemData extends StarSystemDataBase {
    private final JsonNode node;
    private final SUPPORT_VERSIONS version;

    public EDDNSystemData(JsonNode node, SUPPORT_VERSIONS version) {
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
        JsonNode n = node.get("systemName");
        if (n != null) name = n.asText();
        if (name == null){
            throw new ImportDataError("EDDN message don't have system name");
        }
        return name;
    }

    @Nullable
    @Override
    public Collection<StationData> getStations() {
        Collection<StationData> stations = new ArrayList<>();
        stations.add(new EDDNStationData(node, version));
        return stations;
    }
}

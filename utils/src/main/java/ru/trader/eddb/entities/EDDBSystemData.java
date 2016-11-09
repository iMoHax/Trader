package ru.trader.eddb.entities;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.eddb.EDDBConverter;
import ru.trader.eddb.EDDBParser;
import ru.trader.store.imp.ImportDataError;
import ru.trader.store.imp.entities.StarSystemDataBase;
import ru.trader.store.imp.entities.StationData;

import java.util.Collection;


public class EDDBSystemData extends StarSystemDataBase {
    private final static Logger LOG = LoggerFactory.getLogger(EDDBSystemData.class);

    private final JsonNode node;
    private final EDDBParser.FILE_TYPE type;

    public EDDBSystemData(JsonNode node, EDDBParser.FILE_TYPE type) {
        this.node = node;
        this.type = type;
    }

    @Override
    public String getName() {
        String name = null;
        JsonNode n = node.get("name");
        if (n != null) name = n.asText();
        if (name == null){
            throw new ImportDataError("EDDB entry don't have system name");
        }
        return name;
    }

    @Override
    public double getX() {
        if (!type.hasSystemData()) return super.getX();
        Double x = null;
        JsonNode n = node.get("x");
        if (n != null) x = n.asDouble();
        if (x == null){
            throw new ImportDataError("EDDB entry don't have X coordination");
        }
        return x;
    }

    @Override
    public double getY() {
        if (!type.hasSystemData()) return super.getY();
        Double y = null;
        JsonNode n = node.get("y");
        if (n != null) y = n.asDouble();
        if (y == null){
            throw new ImportDataError("EDDB entry don't have Y coordination");
        }
        return y;
    }

    @Override
    public double getZ() {
        if (!type.hasSystemData()) return super.getZ();
        Double z = null;
        JsonNode n = node.get("z");
        if (n != null) z = n.asDouble();
        if (z == null){
            throw new ImportDataError("EDDB entry don't have Z coordination");
        }
        return z;
    }

    @Nullable
    @Override
    public Long getPopulation() {
        if (!type.hasSystemData()) return super.getPopulation();
        Long population = null;
        JsonNode n = node.get("population");
        if (n != null) population = n.asLong();
        if (population == null){
            throw new ImportDataError("EDDB entry don't have population");
        }
        return population;
    }

    @Nullable
    @Override
    public FACTION getFaction() {
        if (!type.hasSystemData()) return super.getFaction();
        FACTION faction = null;
        JsonNode n = node.get("allegiance_id");
        if (n != null){
            faction = EDDBConverter.asAlliance(n.asInt());
            if (faction == null){
                LOG.warn("Unknown allegiance eddb id: {}", n.asInt());
            }
        }
        return faction;
    }

    @Nullable
    @Override
    public GOVERNMENT getGovernment() {
        if (!type.hasSystemData()) return super.getGovernment();
        GOVERNMENT government = null;
        JsonNode n = node.get("government_id");
        if (n != null){
            government = EDDBConverter.asGovernment(n.asInt());
            if (government == null){
                LOG.warn("Unknown government eddb id: {}", n.asInt());
            }
        }
        return government;
    }

    @Nullable
    @Override
    public Collection<StationData> getStations() {
        if (!type.hasStationsData()) return super.getStations();
        return super.getStations();
    }
}

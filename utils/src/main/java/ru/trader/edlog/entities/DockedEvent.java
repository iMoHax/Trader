package ru.trader.edlog.entities;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;
import ru.trader.core.ECONOMIC_TYPE;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.STATION_TYPE;
import ru.trader.edlog.EDConverter;
import ru.trader.store.imp.entities.StarSystemData;
import ru.trader.store.imp.entities.StarSystemDataBase;
import ru.trader.store.imp.entities.StationData;
import ru.trader.store.imp.entities.StationDataBase;

import java.util.Collection;
import java.util.Collections;

public class DockedEvent{
    private final JsonNode node;

    public DockedEvent(JsonNode node) {
        this.node = node;
    }

    public String getStation(){
        JsonNode n = node.get("StationName");
        if (n == null){
            throw new IllegalArgumentException("Event Docked don't have StationName attribute");
        }
        return n.asText();
    }

    public String getStarSystem(){
        JsonNode n = node.get("StarSystem");
        if (n == null){
            throw new IllegalArgumentException("Event Docked don't have StarSystem attribute");
        }
        return n.asText();
    }

    @Nullable
    public STATION_TYPE getStationType(){
        JsonNode n = node.get("StationType");
        return n != null ? EDConverter.asStationType(n.asText()) : null;
    }

    @Nullable
    public GOVERNMENT getGovernment(){
        JsonNode n = node.get("StationGovernment");
        return n != null ? EDConverter.asGovernment(n.asText()) : null;
    }

    @Nullable
    public FACTION getAllegiance(){
        JsonNode n = node.get("StationAllegiance");
        return n != null ? EDConverter.asAllegiance(n.asText()) : null;
    }

    @Nullable
    public ECONOMIC_TYPE getEconomic(){
        JsonNode n = node.get("StationEconomy");
        return n != null ? EDConverter.asEconomic(n.asText()) : null;
    }

    public StarSystemData asImportData(){
        final Collection<StationData> stationData = Collections.singleton(asStationData());
        return new StarSystemDataBase() {

            @Override
            public String getName() {
                return DockedEvent.this.getStarSystem();
            }

            @Nullable
            @Override
            public Collection<StationData> getStations() {
                return stationData;
            }
        };
    }

    private StationData asStationData(){
        return new StationDataBase(){

            @Override
            public String getName() {
                return DockedEvent.this.getStation();
            }

            @Nullable
            @Override
            public STATION_TYPE getType() {
                return DockedEvent.this.getStationType();
            }

            @Nullable
            @Override
            public FACTION getFaction() {
                return DockedEvent.this.getAllegiance();
            }

            @Nullable
            @Override
            public GOVERNMENT getGovernment() {
                return DockedEvent.this.getGovernment();
            }

            @Nullable
            @Override
            public ECONOMIC_TYPE getEconomic() {
                return DockedEvent.this.getEconomic();
            }
        };
    }
}

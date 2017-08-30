package ru.trader.edlog.entities;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;
import ru.trader.core.*;
import ru.trader.edlog.EDConverter;
import ru.trader.store.imp.entities.StarSystemData;
import ru.trader.store.imp.entities.StarSystemDataBase;

public class FSDJumpEvent {
    private final JsonNode node;

    public FSDJumpEvent(JsonNode node) {
        this.node = node;
    }

    public String getStarSystem(){
        JsonNode n = node.get("StarSystem");
        if (n == null){
            throw new IllegalArgumentException("Event FSDJump don't have StarSystem attribute");
        }
        return n.asText();
    }

    public double getX(){
        JsonNode n = node.get("StarPos");
        if (n == null || !n.isArray() || n.size() < 3){
            throw new IllegalArgumentException("Event FSDJump don't have correct StarPos attribute");
        }
        return n.get(0).asDouble();
    }

    public double getY(){
        JsonNode n = node.get("StarPos");
        if (n == null || !n.isArray() || n.size() < 3){
            throw new IllegalArgumentException("Event FSDJump don't have correct StarPos attribute");
        }
        return n.get(1).asDouble();
    }

    public double getZ(){
        JsonNode n = node.get("StarPos");
        if (n == null || !n.isArray() || n.size() < 3){
            throw new IllegalArgumentException("Event FSDJump don't have correct StarPos attribute");
        }
        return n.get(2).asDouble();
    }


    @Nullable
    public GOVERNMENT getGovernment(){
        JsonNode n = node.get("SystemGovernment");
        return n != null ? EDConverter.asGovernment(n.asText()) : null;
    }

    @Nullable
    public FACTION getAllegiance(){
        JsonNode n = node.get("SystemAllegiance");
        return n != null ? EDConverter.asAllegiance(n.asText()) : null;
    }

    @Nullable
    public ECONOMIC_TYPE getEconomic(){
        JsonNode n = node.get("SystemEconomy");
        return n != null ? EDConverter.asEconomic(n.asText()) : null;
    }

    @Nullable
    public POWER getPower(){
        JsonNode n = node.get("Powers");
        if (n == null) return null;
        if (n.isArray()){
            n = n.get(0);
        }
        return n != null ? EDConverter.asPower(n.asText()) : null;
    }

    @Nullable
    public POWER_STATE getPowerState(){
        JsonNode n = node.get("PowerplayState");
        return n != null ? EDConverter.asPowerState(n.asText()) : null;
    }


    public StarSystemData asImportData(){
        return new StarSystemDataBase() {
            @Override
            public String getName() {
                return FSDJumpEvent.this.getStarSystem();
            }

            @Override
            public double getX() {
                return FSDJumpEvent.this.getX();
            }

            @Override
            public double getY() {
                return FSDJumpEvent.this.getY();
            }

            @Override
            public double getZ() {
                return FSDJumpEvent.this.getZ();
            }

            @Nullable
            @Override
            public FACTION getFaction() {
                return FSDJumpEvent.this.getAllegiance();
            }

            @Nullable
            @Override
            public GOVERNMENT getGovernment() {
                return FSDJumpEvent.this.getGovernment();
            }

            @Nullable
            @Override
            public POWER getPower() {
                return FSDJumpEvent.this.getPower();
            }

            @Nullable
            @Override
            public POWER_STATE getPowerState() {
                return FSDJumpEvent.this.getPowerState();
            }
        };

    }
}

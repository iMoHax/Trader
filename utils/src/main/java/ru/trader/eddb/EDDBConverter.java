package ru.trader.eddb;


import org.jetbrains.annotations.Nullable;
import ru.trader.core.*;

public class EDDBConverter {

    @Nullable
    public static FACTION asAlliance(int id){
        switch (id){
            case 1: return FACTION.ALLIANCE;
            case 2: return FACTION.EMPIRE;
            case 3: return FACTION.FEDERATION;
            case 4: return FACTION.INDEPENDENT;
            case 5: return FACTION.NONE;
        }
        return null;
    }

    @Nullable
    public static GOVERNMENT asGovernment(int id){
        switch (id){
            case 16: return GOVERNMENT.ANARCHY;
            case 32: return GOVERNMENT.COMMUNISM;
            case 48: return GOVERNMENT.CONFEDERACY;
            case 64: return GOVERNMENT.CORPORATE;
            case 80: return GOVERNMENT.COOPERATIVE;
            case 96: return GOVERNMENT.DEMOCRACY;
            case 112: return GOVERNMENT.DICTATORSHIP;
            case 128: return GOVERNMENT.FEUDAL;
            case 133: return GOVERNMENT.IMPERIAL;
            case 144: return GOVERNMENT.PATRONAGE;
            case 150: return GOVERNMENT.PRISON_COLONY;
            case 160: return GOVERNMENT.THEOCRACY;
            case 176: return GOVERNMENT.NONE;
        }
        return null;
    }


    @Nullable
    public static ECONOMIC_TYPE asEconomic(int id){
    switch (id){
        case 1: return ECONOMIC_TYPE.AGRICULTURE;
        case 2: return ECONOMIC_TYPE.EXTRACTION;
        case 3: return ECONOMIC_TYPE.HIGH_TECH;
        case 4: return ECONOMIC_TYPE.INDUSTRIAL;
        case 5: return ECONOMIC_TYPE.MILITARY;
        case 6: return ECONOMIC_TYPE.REFINERY;
        case 7: return ECONOMIC_TYPE.SERVICE;
        case 8: return ECONOMIC_TYPE.TERRAFORMING;
        case 9: return ECONOMIC_TYPE.TOURISM;
        case 11: return ECONOMIC_TYPE.COLONY;
        case 10: return ECONOMIC_TYPE.NONE;
    }
    return null;
}

    @Nullable
    public static POWER asPower(int id) {
        switch (id) {
            case 1:
                return POWER.MAHON;
            case 2:
                return POWER.WINTERS;
            case 3:
                return POWER.HUDSON;
            case 4:
                return POWER.TORVAL;
            case 5:
                return POWER.LAVIGNY_DUVAL;
            case 6:
                return POWER.DUVAL;
            case 7:
                return POWER.PATREUS;
            case 8:
                return POWER.DELAINE;
            case 9:
                return POWER.YONG_RUI;
            case 10:
                return POWER.ANTAL;
            case 11:
                return POWER.GROM;
        }
        return null;
    }

    @Nullable
    public static POWER_STATE asPowerState(int id){
        switch (id){
            case 16: return POWER_STATE.CONTROL;
            case 32: return POWER_STATE.EXPLOITED;
            case 64: return POWER_STATE.EXPANSION;
        }
        return null;
    }
}

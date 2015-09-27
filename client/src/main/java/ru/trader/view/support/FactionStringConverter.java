package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.core.FACTION;


public class FactionStringConverter extends StringConverter<FACTION> {

    @Override
    public String toString(FACTION faction) {
        return toLocalizationString(faction);
    }

    @Override
    public FACTION fromString(String faction) {
        return FACTION.valueOf(faction);
    }

    public static String toLocalizationString(FACTION faction){
        if (faction == null) return null;
        return Localization.getString("faction." + faction.toString(), faction.toString());
    }
}

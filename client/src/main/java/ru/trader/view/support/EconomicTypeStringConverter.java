package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.core.ECONOMIC_TYPE;


public class EconomicTypeStringConverter extends StringConverter<ECONOMIC_TYPE> {

    @Override
    public String toString(ECONOMIC_TYPE economic) {
        return toLocalizationString(economic);
    }

    @Override
    public ECONOMIC_TYPE fromString(String economic) {
        return ECONOMIC_TYPE.valueOf(economic);
    }

    public static String toLocalizationString(ECONOMIC_TYPE economic){
        if (economic == null) return null;
        return Localization.getString("economic." + economic.toString(), economic.toString());
    }
}

package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.core.STATION_TYPE;


public class StationTypeStringConverter extends StringConverter<STATION_TYPE> {

    @Override
    public String toString(STATION_TYPE type) {
        return toLocalizationString(type);
    }

    @Override
    public STATION_TYPE fromString(String type) {
        return STATION_TYPE.valueOf(type);
    }

    public static String toLocalizationString(STATION_TYPE type){
        if (type == null) return null;
        return Localization.getString("station." + type.toString(), type.toString());
    }
}

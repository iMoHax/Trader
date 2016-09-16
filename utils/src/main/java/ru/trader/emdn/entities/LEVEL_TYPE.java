package ru.trader.emdn.entities;

public enum LEVEL_TYPE {
    LOW("Low"), MEDIUM("Med"), HIGH("High");

    private final String jsonText;

    private LEVEL_TYPE(String jsonText) {
        this.jsonText = jsonText;
    }

    public String toJSON(){
        return jsonText;
    }

    public static LEVEL_TYPE fromJSON(String text){
        for (LEVEL_TYPE level : LEVEL_TYPE.values()) {
            if (level.jsonText.equals(text)) return level;
        }
        return null;
    }


}

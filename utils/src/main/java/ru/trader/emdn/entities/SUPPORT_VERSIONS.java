package ru.trader.emdn.entities;

public enum SUPPORT_VERSIONS {
    V1_SHIPYARD("http://schemas.elite-markets.net/eddn/shipyard/1"),
    V2_SHIPYARD("http://schemas.elite-markets.net/eddn/shipyard/2"),
    V1("http://schemas.elite-markets.net/eddn/commodity/1"),
    V2("http://schemas.elite-markets.net/eddn/commodity/2"),
    V3("http://schemas.elite-markets.net/eddn/commodity/3");

    private final String schema;

    public String getSchema() {
        return schema;
    }

    private SUPPORT_VERSIONS(String schema) {
        this.schema = schema;
    }

    public static SUPPORT_VERSIONS getVersion(String schema){
        for (SUPPORT_VERSIONS parser : SUPPORT_VERSIONS.values()) {
            if (parser.schema.equals(schema)) return parser;
        }
        return null;
    }
}
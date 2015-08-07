package ru.trader.edce.entities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Starport {
    private long id;
    private String name;
    private String faction;
    private List<Commodity> commodities = new ArrayList<>();
    private Map<String, Module> modules = new LinkedHashMap<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public List<Commodity> getCommodities() {
        return commodities;
    }

    public void setCommodities(List<Commodity> commodities) {
        this.commodities = commodities;
    }

    public Map<String, Module> getModules() {
        return modules;
    }

    public void setModules(Map<String, Module> modules) {
        this.modules = modules;
    }
}

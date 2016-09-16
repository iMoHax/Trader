package ru.trader.emdn.entities;

public class Item {
    private final String name;
    private Long id;
    private final long buyPrice;
    private final long supply;
    private LEVEL_TYPE supplyLevel;
    private final long sellPrice;
    private final long demand;
    private LEVEL_TYPE demandLevel;

    public Item(String name, long buyPrice, long supply, long sellPrice, long demand) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.supply = supply;
        this.sellPrice = sellPrice;
        this.demand = demand;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getBuyPrice() {
        return buyPrice;
    }

    public long getSupply() {
        return supply;
    }

    public LEVEL_TYPE getSupplyLevel() {
        return supplyLevel;
    }

    public void setSupplyLevel(LEVEL_TYPE supplyLevel) {
        this.supplyLevel = supplyLevel;
    }

    public long getSellPrice() {
        return sellPrice;
    }

    public long getDemand() {
        return demand;
    }

    public LEVEL_TYPE getDemandLevel() {
        return demandLevel;
    }

    public void setDemandLevel(LEVEL_TYPE demandLevel) {
        this.demandLevel = demandLevel;
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", buyPrice=" + buyPrice +
                ", supply=" + supply +
                ", supplyLevel=" + supplyLevel +
                ", sellPrice=" + sellPrice +
                ", demand=" + demand +
                ", demandLevel=" + demandLevel +
                "} " + super.toString();
    }
}

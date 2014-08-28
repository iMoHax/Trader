package ru.trader.emdn;

public class ItemData {
    private final String name;
    private double buy;
    private double sell;
    private long demand;
    private long stock;


    public ItemData(String name) {
        this.name = name;
    }

    public void setSell(double price, long count){
        sell = price;
        demand = count;
    }

    public void setBuy(double price, long count){
        buy = price;
        stock = count;
    }

    public double getBuy() {
        return buy;
    }

    public double getSell() {
        return sell;
    }

    public long getDemand() {
        return demand;
    }

    public long getStock() {
        return stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemData)) return false;
        ItemData itemData = (ItemData) o;
        return !(name != null ? !name.equals(itemData.name) : itemData.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(name);
        sb.append(" buy=").append(buy);
        sb.append(" (").append(stock).append(")");
        sb.append(" sell=").append(sell);
        sb.append(" (").append(demand).append(")");
        sb.append('}');
        return sb.toString();
    }
}

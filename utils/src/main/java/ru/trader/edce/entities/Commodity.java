package ru.trader.edce.entities;


public class Commodity {
    private long id;
    private String name;
    private long buyPrice;
    private long sellPrice;
    private int demandBracket;
    private int stockBracket;
    private long stock;
    private long demand;
    private String categoryname;

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

    public long getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(long buyPrice) {
        this.buyPrice = buyPrice;
    }

    public long getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(long sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getDemandBracket() {
        return demandBracket;
    }

    public void setDemandBracket(int demandBracket) {
        this.demandBracket = demandBracket;
    }

    public int getStockBracket() {
        return stockBracket;
    }

    public void setStockBracket(int stockBracket) {
        this.stockBracket = stockBracket;
    }

    public long getStock() {
        return stockBracket != 0 ? stock : 0;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    public long getDemand() {
        return demandBracket != 0 ? demand : 0;
    }

    public void setDemand(long demand) {
        this.demand = demand;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }
}

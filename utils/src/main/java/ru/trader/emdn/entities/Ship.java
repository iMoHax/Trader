package ru.trader.emdn.entities;

public class Ship {
    private final String name;
    private Long price;
    private Long id;

    public Ship(String name) {
        this.name = name;
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

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", price=" + price +
                "} ";
    }
}

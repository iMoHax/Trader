package ru.trader.emdn.entities;

public class StarSystem {
    private final String name;
    private Long id;
    private Long address;

    public StarSystem(String name) {
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

    public Long getAddress() {
        return address;
    }

    public void setAddress(Long address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "StarSystem{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", address=" + address +
                "} ";
    }
}

package ru.trader.emdn.entities;

public class Station {
    private final String name;
    private Long id;

    public Station(String name) {
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

    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", id=" + id +
                "} ";
    }
}

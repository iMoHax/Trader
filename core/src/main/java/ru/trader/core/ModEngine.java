package ru.trader.core;

import java.util.Objects;

public class ModEngine extends Engine {
    private final double optMass;

    public ModEngine(int clazz, char rating) {
        super(clazz, rating);
        optMass = super.getOptMass();
    }

    public ModEngine(int clazz, char rating, double optMass) {
        super(clazz, rating);
        this.optMass = optMass;
    }

    public ModEngine(Engine engine, double optMass) {
        super(engine.getClazz(), engine.getRating());
        this.optMass = optMass;
    }

    @Override
    public double getOptMass() {
        return optMass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModEngine)) return false;
        if (!super.equals(o)) return false;
        ModEngine modEngine = (ModEngine) o;
        return Objects.equals(optMass, modEngine.optMass);
    }
}

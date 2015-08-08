package ru.trader.edce.entities;

import java.util.Objects;

public class Slot {
    private Module module;

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return Objects.equals(module, slot.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module);
    }
}

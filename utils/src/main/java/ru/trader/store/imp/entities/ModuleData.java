package ru.trader.store.imp.entities;

import org.jetbrains.annotations.Nullable;

public interface ModuleData {

    @Nullable
    Long getId();
    String getName();
    @Nullable
    String getGroup();
    long getPrice();

}

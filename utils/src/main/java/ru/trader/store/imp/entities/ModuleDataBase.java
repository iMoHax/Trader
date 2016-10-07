package ru.trader.store.imp.entities;

import org.jetbrains.annotations.Nullable;

public abstract class ModuleDataBase implements ModuleData {
    @Override
    public Long getId() {
        return null;
    }

    @Nullable
    @Override
    public String getGroup() {
        return null;
    }

}

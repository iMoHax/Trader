package ru.trader.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class AbstractItem implements Item {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractItem.class);
    private AbstractMarket market;

    protected abstract void updateName(String name);

    protected final void setMarket(AbstractMarket market){
        assert this.market == null;
        this.market = market;
    }

    @Override
    public final void setName(String name){
        if (market != null){
            LOG.debug("Change name of item {} to {}", this, name);
            market.updateName(this, name);
            market.setChange(true);
        } else {
            updateName(name);
        }
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public int compareTo(@NotNull Item other){
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        String name = getName();
        String otherName = other.getName();
        return name != null ? otherName != null ? name.compareTo(otherName) : -1 : 0;
    }

}

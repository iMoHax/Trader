package ru.trader.store.simple;

import ru.trader.core.AbstractItem;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.Group;

import java.util.Collection;
import java.util.EnumSet;

public class SimpleItem extends AbstractItem {
    private String name;
    private Group group;
    private final EnumSet<GOVERNMENT> gIllegals = EnumSet.noneOf(GOVERNMENT.class);
    private final EnumSet<FACTION> fIllegals = EnumSet.noneOf(FACTION.class);

    public SimpleItem(String name) {
        this.name = name;
    }

    public SimpleItem(String name, Group group) {
        this.name = name;
        setGroup(group);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void updateName(String name) {
        this.name = name;
    }

    @Override
    protected void updateIllegalState(FACTION faction, boolean illegal) {
        if (illegal) fIllegals.add(faction);
            else fIllegals.remove(faction);
    }

    @Override
    public Collection<FACTION> getIllegalFactions() {
        return fIllegals;
    }

    @Override
    protected void updateIllegalState(GOVERNMENT government, boolean illegal) {
        if (illegal) gIllegals.add(government);
            else gIllegals.remove(government);
    }

    @Override
    public Collection<GOVERNMENT> getIllegalGovernments() {
        return gIllegals;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    protected void setGroup(Group group) {
        this.group = group;
    }
}

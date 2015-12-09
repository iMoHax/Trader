package ru.trader.store.berkeley;

import ru.trader.core.AbstractItem;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.Group;
import ru.trader.store.berkeley.entities.BDBItem;

public class ItemProxy extends AbstractItem {
    private final BDBItem item;
    private final BDBStore store;
    private Group group;

    public ItemProxy(BDBItem item, BDBStore store) {
        this.item = item;
        this.store = store;
    }

    protected long getId(){
        return item.getId();
    }

    protected BDBItem getEntity(){
        return item;
    }

    @Override
    protected void updateName(String name) {
        item.setName(name);
        store.getItemAccessor().update(item);
    }

    @Override
    public String getName() {
        return item.getName();
    }

    @Override
    protected void updateIllegalState(FACTION faction, boolean illegal) {
        item.setIllegal(faction, illegal);
        store.getItemAccessor().update(item);
    }

    @Override
    public boolean isIllegal(FACTION faction) {
        return item.isIllegal(faction);
    }

    @Override
    protected void updateIllegalState(GOVERNMENT government, boolean illegal) {
        item.setIllegal(government, illegal);
        store.getItemAccessor().update(item);
    }

    @Override
    public boolean isIllegal(GOVERNMENT government) {
        return item.isIllegal(government);
    }

    @Override
    public Group getGroup() {
        if (group == null){
            group = store.getGroupAccessor().get(item.getGroupId());
        }
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemProxy)) return false;

        ItemProxy itemProxy = (ItemProxy) o;

        if (!item.equals(itemProxy.item)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }
}

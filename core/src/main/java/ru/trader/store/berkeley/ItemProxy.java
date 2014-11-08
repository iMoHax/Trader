package ru.trader.store.berkeley;

import ru.trader.core.AbstractItem;
import ru.trader.core.Group;
import ru.trader.store.berkeley.entities.BDBItem;

public class ItemProxy extends AbstractItem {
    private final BDBItem item;
    private final BDBStore store;
    private final Group group;

    public ItemProxy(BDBItem item, BDBStore store) {
        this.item = item;
        this.group = store.getGroupAccessor().get(item.getGroupId());
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
    public Group getGroup() {
        return group;
    }

}

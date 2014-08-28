package ru.trader.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Item;
import ru.trader.view.support.Localization;

public class ItemModel{
    private final static Logger LOG = LoggerFactory.getLogger(ItemModel.class);
    private final Item item;
    private final MarketModel market;
    private StringProperty name;

    ItemModel(Item item, MarketModel market) {
        this.item = item;
        this.market = market;
    }

    public String getName() {return name != null ? name.get() : item.getName();}

    public String getId() {return item.getName();}

    public void setName(String value) {
        LOG.info("Change name of item {} to {}", item, name);
        market.updateName(this, value);
        if (name != null) name.set(value);
    }

    public ReadOnlyStringProperty nameProperty() {
        if (name == null) {
            String lName = Localization.getString("item."+item.getName(), item.getName());
            name = new SimpleStringProperty(lName);
        }
        return name;
    }

    @Override
    public String toString() {
        if (LOG.isTraceEnabled()){
            final StringBuilder sb = new StringBuilder("ItemModel{");
            sb.append("nameProp=").append(name);
            sb.append(", item=").append(super.toString());
            sb.append('}');
            return sb.toString();
        }
        return item.toString();
    }

    Item getItem() {
        return item;
    }

}

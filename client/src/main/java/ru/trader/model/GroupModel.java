package ru.trader.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ru.trader.core.Group;
import ru.trader.view.support.Localization;

public class GroupModel {

    private final Group group;
    private StringProperty name;


    GroupModel(Group group) {
        this.group = group;
    }

    Group getGroup(){
        return group;
    }

    public String getId() {
        return group.getName();
    }

    public String getName() {
        return name != null ? name.get() : buildName();
    }

    public ReadOnlyStringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty(buildName());
        }
        return name;
    }

    private String buildName(){
        return Localization.getString("item.group." + group.getName(), group.getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}

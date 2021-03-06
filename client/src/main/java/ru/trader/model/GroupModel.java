package ru.trader.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ru.trader.core.Group;
import ru.trader.view.support.Localization;

import java.util.Objects;

public class GroupModel implements Comparable<GroupModel> {

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

    void updateName(){
        if (name != null){
            name.setValue(buildName());
        }
    }

    @Override
    public int compareTo(GroupModel other) {
        int cmp = group.getType().compareTo(other.group.getType());
        if (cmp != 0) return cmp;
        return getName().compareTo(other.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupModel that = (GroupModel) o;
        return Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group);
    }

    @Override
    public String toString() {
        return getName();
    }
}

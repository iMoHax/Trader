package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import ru.trader.core.GROUP_TYPE;


@Entity
public class Group {
    @PrimaryKey
    private String name;

    private GROUP_TYPE type;

    private Group() {
    }

    public Group(String name, GROUP_TYPE type) {
        this.name = name;
        this.type = type;
    }
}

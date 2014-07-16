package ru.trader.model.support;

import com.sun.javafx.collections.ImmutableObservableList;
import javafx.beans.Observable;
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class BindingsHelper {

    public static <T,M extends Observable> ObservableList<M> deepObservableList(Collection<T> entries, Function<T, M> convert){
        return deepBind(observableList(entries, convert));
    }

    public static <T,M> ObservableList<M> observableList(Collection<T> entries, Function<T, M> convert){
        List<M> list = new ArrayList<>(entries.size());
        entries.forEach((v)->list.add(convert.apply(v)));
        return FXCollections.observableList(list);
    }

    public static <T extends Observable> ObservableList<T> deepBind(final ObservableList<T> list){
        if ((list == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new ListBinding<T>() {
            {
                super.bind(list);
                list.addListener((ListChangeListener<T>) c -> {
                    while (c.next()) {
                            if (c.wasAdded())
                                for (T item : c.getRemoved()) {
                                    super.bind(item);
                            } else if (c.wasRemoved()){
                                for (T item : c.getAddedSubList()) {
                                    super.unbind(item);
                            }
                        }
                    }
                });

            }

            @Override
            public void dispose() {
                list.forEach((v) -> super.unbind(v));
                super.unbind(list);
            }

            @Override
            protected ObservableList<T> computeValue() {
                return list;
            }

            @Override
            public javafx.collections.ObservableList<?> getDependencies() {
                ArrayList<Observable> dependencies = new ArrayList<>(list.size());
                dependencies.addAll(list);
                dependencies.add(list);
                return new ImmutableObservableList<>((Observable[]) dependencies.toArray());
            }
        };
    }

}

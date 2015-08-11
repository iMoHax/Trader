package ru.trader.model.support;

import com.sun.javafx.collections.ImmutableObservableList;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BinaryOperator;
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
            private final ListChangeListener<T> listListener = c -> {
                while (c.next()) {
                    if (c.wasAdded())
                        for (T item : c.getAddedSubList()) {
                            super.bind(item);
                        } else if (c.wasRemoved()){
                        for (T item : c.getRemoved()) {
                            super.unbind(item);
                        }
                    }
                }
            };

            {
                super.bind(list);
                list.addListener(listListener);
                list.forEach(super::bind);

            }

            @Override
            public void dispose() {
                list.forEach(super::unbind);
                list.removeListener(listListener);
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

    public static <T> SortedList<T> sortedObservableList(List<T> list){
        return (list instanceof ObservableList) ? new SortedList<>((ObservableList<? extends T>) list) : new SortedList<>(FXCollections.observableList(list));
    }

    public static <T> void setTableViewItems(TableView<T> table, List<T> items){
        ObservableList<T> list = table.getItems();
        SortedList<T> sList = sortedObservableList(items);
        if (list instanceof SortedList){
            ((SortedList)list).comparatorProperty().unbind();
        }
        table.setItems(sList);
        sList.comparatorProperty().bind(table.comparatorProperty());
    }

    public static <T> DoubleBinding group(final BinaryOperator<Double> operation, final Function<T, ObservableNumberValue> convert, final List<T> list){
        if ((list == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new DoubleBinding(){
            private final ListChangeListener<T> listListener = c -> {
                while (c.next()) {
                    if (c.wasAdded())
                        for (T item : c.getAddedSubList()) {
                            super.bind(convert.apply(item));
                    } else if (c.wasRemoved()){
                        for (T item : c.getRemoved()) {
                            super.unbind(convert.apply(item));
                        }
                    }
                }
            };

            {
                if (list instanceof ObservableList){
                    ObservableList<T> oList = (ObservableList<T>) list;
                    super.bind(oList);
                    oList.addListener(listListener);
                }
                list.forEach(v -> super.bind(convert.apply(v)));
            }

            @Override
            public void dispose() {
                list.forEach(v -> super.unbind(convert.apply(v)));
                if (list instanceof ObservableList){
                    ObservableList<T> oList = (ObservableList<T>) list;
                    oList.removeListener(listListener);
                    super.unbind(oList);
                }
            }

            @Override
            protected double computeValue() {
                double res = 0;
                for (T item : list) {
                    res = operation.apply(res, convert.apply(item).doubleValue());
                }
                return res;
            }

            @Override
            public javafx.collections.ObservableList<?> getDependencies() {
                ArrayList<Observable> dependencies = new ArrayList<>(list.size());
                list.forEach(v -> dependencies.add(convert.apply(v)));
                if (list instanceof ObservableList){
                    dependencies.add((ObservableList)list);
                }
                return new ImmutableObservableList<>((Observable[]) dependencies.toArray());
            }
        };
    }

}

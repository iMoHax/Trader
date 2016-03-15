package ru.trader.view.support.cells;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public abstract class DecoratedCellFactory<S, T> implements Callback<TableColumn<S,T>, TableCell<S,T>> {
    private final Callback<TableColumn<S,T>, TableCell<S,T>> decorated;

    public DecoratedCellFactory() {
        //noinspection unchecked
        this((Callback) TableColumn.DEFAULT_CELL_FACTORY);
    }

    public DecoratedCellFactory(Callback<TableColumn<S, T>, TableCell<S, T>> decorated) {
        this.decorated = decorated;
    }

    protected abstract void doStyle(TableCell<S, T> cell, S entry, T item);

    @Override
    public final TableCell<S, T> call(TableColumn<S, T> param) {
        TableCell<S,T> cell = decorated.call(param);
        cell.itemProperty().addListener(new ItemChangeListener(cell));
        return cell;
    }

    private class ItemChangeListener implements ChangeListener<T> {
        private final TableCell<S, T> cell;

        private ItemChangeListener(TableCell<S, T> cell) {
            this.cell = cell;
        }

        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            @SuppressWarnings("unchecked")
            S entry = (S) cell.getTableRow().getItem();
            doStyle(cell, entry, newValue);
        }
    }

}

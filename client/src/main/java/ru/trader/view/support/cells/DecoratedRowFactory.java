package ru.trader.view.support.cells;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public abstract class DecoratedRowFactory<S> implements Callback<TableView<S>, TableRow<S>> {
    private final Callback<TableView<S>, TableRow<S>> decorated;
    private final static Callback<TableView<?>, TableRow<?>> DEFAULT_ROW_FACTORY = row -> new TableRow<>();

    public DecoratedRowFactory() {
        //noinspection unchecked
        this((Callback) DEFAULT_ROW_FACTORY);
    }

    public DecoratedRowFactory(Callback<TableView<S>, TableRow<S>> decorated) {
        this.decorated = decorated;
    }

    protected abstract void doStyle(TableRow<S> row, S entry);

    @Override
    public final TableRow<S> call(TableView<S> param) {
        TableRow<S> row = decorated.call(param);
        row.itemProperty().addListener(new ItemChangeListener(row));
        return row;
    }

    private class ItemChangeListener implements ChangeListener<S> {
        private final TableRow<S> row;

        private ItemChangeListener(TableRow<S> row) {
            this.row = row;
        }

        @Override
        public void changed(ObservableValue<? extends S> observable, S oldValue, S newValue) {
            doStyle(row, newValue);
        }
    }

}

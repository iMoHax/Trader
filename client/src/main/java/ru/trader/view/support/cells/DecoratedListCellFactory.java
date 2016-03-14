package ru.trader.view.support.cells;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public abstract class DecoratedListCellFactory<T> implements Callback<ListView<T>, ListCell<T>> {
    private final Callback<ListView<T>, ListCell<T>> decorated;
    private final static Callback<ListView<?>, ListCell<?>> DEFAULT_CELL_FACTORY = cell -> new ListCell<Object>() {
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item instanceof Node) {
                setText(null);
                Node currentNode = getGraphic();
                Node newNode = (Node) item;
                if (currentNode == null || ! currentNode.equals(newNode)) {
                    setGraphic(newNode);
                }
            } else {
                setText(item == null ? "null" : item.toString());
                setGraphic(null);
            }
        }
    };

    public DecoratedListCellFactory() {
        //noinspection unchecked
        this((Callback) DEFAULT_CELL_FACTORY);
    }

    public DecoratedListCellFactory(Callback<ListView<T>, ListCell<T>> decorated) {
        this.decorated = decorated;
    }

    abstract void doStyle(ListCell<T> cell, T item);

    @Override
    public final ListCell<T> call(ListView<T> param) {
        ListCell<T> cell = decorated.call(param);
        cell.itemProperty().addListener(new ItemChangeListener(cell));
        return cell;
    }

    private class ItemChangeListener implements ChangeListener<T> {
        private final ListCell<T> cell;

        private ItemChangeListener(ListCell<T> cell) {
            this.cell = cell;
        }

        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            doStyle(cell, newValue);
        }
    }

}

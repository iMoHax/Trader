package ru.trader.view.support.cells;


import impl.org.controlsfx.skin.CheckComboBoxSkin;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

import java.util.Collection;
import java.util.Optional;

public class CheckComboBoxTableCell<S,T> extends TableCell<S, Collection<T>> {
    private final CheckComboBox<T> box;
    private final ChangeListener<Boolean> onShowListener;
    private ComboBox comboBox;

    public CheckComboBoxTableCell(final TableColumn<S, Collection<T>> column, final ObservableList<T> choiceList, final StringConverter<T> converter, final CheckedFunction<S, T> onCheckFunction) {
        box = new CheckComboBox<>(choiceList);
        box.setConverter(converter);
        box.disableProperty().bind(column.editableProperty().not());

        onShowListener = (ov, o, n) -> {
            final TableView<S> tableView = getTableView();
            if (n && !isEditing()) {
                tableView.getSelectionModel().select(getTableRow().getIndex());
                tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);
            } else {
                if (!n && isEditing()) {
                    cancelEdit();
                }
            }
        };
        box.skinProperty().addListener(e -> {
            if (comboBox != null){
                comboBox.showingProperty().removeListener(onShowListener);
            }
            comboBox = getComboBox(box);
            if (comboBox != null) {
                comboBox.showingProperty().addListener(onShowListener);
            } else {
                throw new IllegalStateException("Don't found ComboBox in checkComboBox");
            }
        });
        box.getCheckModel().getCheckedItems().addListener((ListChangeListener<T>) c -> {
            if (isEditing()){
                @SuppressWarnings("unchecked")
                S entry = (S) getTableRow().getItem();
                if (entry != null){
                    while (c.next()) {
                        if (c.wasAdded())
                            for (T item : c.getAddedSubList()) {
                                onCheckFunction.apply(entry, item, true);
                        } else if (c.wasRemoved()){
                            for (T item : c.getRemoved()) {
                                onCheckFunction.apply(entry, item, false);
                            }
                        }
                    }
                }
            }
        });
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    public static <S,T> Callback<TableColumn<S,Collection<T>>, TableCell<S,Collection<T>>> forTableColumn(final TableColumn<S, Collection<T>> column, final ObservableList<T> choiceList, final StringConverter<T> converter, final CheckedFunction<S, T> onCheckFunction) {
        return cell -> new CheckComboBoxTableCell<>(column, choiceList, converter, onCheckFunction);
    }

    @Override
    protected void updateItem(Collection<T> items, boolean empty) {
        super.updateItem(items, empty);

        setText(null);
        if (empty || items == null) {
            setGraphic(null);
        } else {
            checkAll(items);
            setGraphic(box);
        }
    }

    private void checkAll(Collection<T> items){
        box.getCheckModel().clearChecks();
        for (T item : items) {
            box.getCheckModel().check(item);
        }
    }


    private static ComboBox getComboBox(CheckComboBox box){
        Skin skin = box.getSkin();
        if (skin instanceof CheckComboBoxSkin){
            Optional node = ((CheckComboBoxSkin) skin).getChildren().stream().findFirst();
            if (node.isPresent() && node.get() instanceof ComboBox){
                return (ComboBox) node.get();
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface CheckedFunction<S, T> {

        void apply(S entry, T item, boolean check);

    }
}
package ru.trader.view.support.cells;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public abstract class EditingCell<S, T> extends TextFieldTableCell<S, T> {


    protected EditingCell(StringConverter<T> converter) {
        super(converter);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && !isEditing()){
            outText();
        }
    }

    protected abstract void outText();

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        outText();
    }
}

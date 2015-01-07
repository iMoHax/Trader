package ru.trader.view.support.cells;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.view.support.ViewUtils;


public class TextFieldCell<S,T> extends TableCell<S,T> {
    private final static Logger LOG = LoggerFactory.getLogger(TextFieldCell.class);

    private TextField textField;
    private final StringConverter<T> converter;


    public TextFieldCell(StringConverter<T> converter) {
        this.converter = converter;

        this.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.PRIMARY)
                if (!isEditing())
                    getTableView().edit(getTableRow().getIndex(), getTableColumn());
        });

    }


    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(final StringConverter<T> converter) {
        return list -> new TextFieldCell<>(converter);
    }

    @Override
    public void startEdit() {
        LOG.trace("Start edit");
        if (! isEditable()) return;
        super.startEdit();
        if (isEditing()){
            if (textField == null) {
                createTextField();
            } else {
                textField.setText(getItemText());
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }
    }

    @Override
    public void updateItem(T item, boolean empty) {
        LOG.trace("Update edit");
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);

        } else {
            if (isEditing()) {
                cancelEdit();
            } else {
                outItem();
            }
        }
    }

    @Override
    public void cancelEdit() {
        LOG.trace("Cancel edit");
        //lost focus
        //crash on scroll, disable
        //if (!isCommit()) commit(false);
        //if (isCommit()) {
            super.cancelEdit();
            outItem();
        //}
    }


    public TextField getTextField(){
        return this.textField;
    }

    private void createTextField(){
        textField = new TextField(getItemText());
        textField.prefWidthProperty().bind(getTableColumn().widthProperty());
        textField.setPadding(Insets.EMPTY);
        textField.setOnAction(event -> {
            if (commit(true)) ViewUtils.editNext(getTableView());
            event.consume();
        });
        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                textField = null;
                cancelEdit();
                t.consume();
            }

        });
    }


    protected String getItemText(){
        return converter.toString(getItem());
    }

    public boolean commit(boolean noSkip) {
        if (isCommit()) return true;
        LOG.trace("Commit text {}", textField.getText());
        try {
            commitEdit(converter.fromString(textField.getText()));
        } catch (NumberFormatException e){
            if (noSkip) {
                Platform.runLater(textField::requestFocus);
                return false;
            }
        }
        textField = null;
        return true;
    }

    protected void outItem(){
        setText(getItemText());
        setGraphic(null);
    }

    protected boolean isCommit(){
        return textField == null;
    }

    public StringConverter<T> getConverter() {
        return converter;
    }
}

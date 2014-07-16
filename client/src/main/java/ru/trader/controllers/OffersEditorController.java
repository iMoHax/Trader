package ru.trader.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.trader.model.ItemModel;
import ru.trader.view.support.NumberField;

import java.util.Optional;

public class OffersEditorController {
    private final Action OK = new AbstractAction("OK") {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }


        @Override
        public void handle(ActionEvent event) {
            Dialog dlg = (Dialog) event.getSource();
            dlg.hide();
        }
    };


    @FXML
    private Label name;

    @FXML
    private NumberField sell;

    @FXML
    private NumberField buy;


    public Optional<DialogResult> showDialog(Parent parent, Parent content, ItemModel item, Number sell, Number buy) {
        name.setText(item.getName());

        this.sell.setValue(sell);
        this.buy.setValue(buy);

        OK.disabledProperty().bind(this.sell.wrongProperty().or(this.buy.wrongProperty()));

        Dialog dlg = new Dialog(parent, "Создание заказов");
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        return Optional.ofNullable(dlg.show() == OK ? new DialogResult() : null);
    }


    public class DialogResult {

        private double _sell;
        private double _buy;

        public DialogResult() {
            _sell = sell.getValue().doubleValue();
            _buy = buy.getValue().doubleValue();
        }

        public double getSell() {
            return _sell;
        }

        public double getBuy() {
            return _buy;
        }
    }
}

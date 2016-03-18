package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import ru.trader.model.GroupModel;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;
import ru.trader.view.support.Localization;

import java.util.Optional;


public class ItemAddController {

    @FXML
    private ComboBox<GroupModel> group;
    @FXML
    private TextField name;

    private Dialog<ItemModel> dlg;
    private MarketModel market;

    @FXML
    private void initialize() {
    }


    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("dialog.item.title"));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(Dialogs.OK, Dialogs.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == Dialogs.OK) {
                return add(market);
            }
            return null;
        });
        dlg.setResizable(false);
    }

    private void fill(MarketModel market) {
        this.market = market;
        group.setItems(market.getGroups());
    }

    private void clear(){
        this.market = null;
        name.clear();
        group.getSelectionModel().clearSelection();
        group.setItems(FXCollections.emptyObservableList());
    }

    public Optional<ItemModel> showDialog(Parent parent, Parent content, MarketModel market) {
        if (dlg == null){
            createDialog(parent, content);
        }
        fill(market);
        Optional<ItemModel> res = dlg.showAndWait();
        clear();
        return res;
    }

    private ItemModel add(MarketModel market){
        GroupModel g = group.getValue();
        String id = name.getText();
        ItemModel res = null;
        if (g != null && id.length() > 0){
            res = market.add(id, g);
        }
        return res;
    }

    public void add(ActionEvent actionEvent) {
        Optional<GroupModel> _group = Screeners.showAddGroup();
        if (_group.isPresent()){
            group.setValue(_group.get());
        }
    }
}

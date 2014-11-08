package ru.trader.controllers;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import ru.trader.model.*;


public class ItemDescController {


    private ItemModel item;

    @FXML
    private ListView<OfferModel> seller;

    @FXML
    private ListView<OfferModel> buyer;

    public void setItemDesc(ItemModel itemDesc){
        item = itemDesc;
        if (popup!=null) popup.setDetachedTitle(item.nameProperty().get());
        fill();
    }

    private void fill(){
        seller.setItems(FXCollections.observableList(item.getSeller()));
        buyer.setItems(FXCollections.observableList(item.getBuyer()));
    }

    private PopOver popup;

    public void popup(Node owner, Parent itemDescScreen) {
        if (popup != null && popup.isShowing()) return;
        if (popup == null) {
            popup = new PopOver(itemDescScreen);
            popup.setDetachedTitle(item.nameProperty().get());
            popup.setAutoHide(true);

        }
        popup.show(owner);
    }

    public void close() {
        if (popup!=null){
            popup.hide(Duration.ZERO);
        }
    }
}

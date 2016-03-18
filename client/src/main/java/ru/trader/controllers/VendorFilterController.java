package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Item;
import ru.trader.core.VendorFilter;
import ru.trader.model.GroupModel;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;
import ru.trader.model.ModelFabric;
import ru.trader.view.support.Localization;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class VendorFilterController {
    private final static Logger LOG = LoggerFactory.getLogger(VendorFilterController.class);

    @FXML
    private GridPane sellCbs;
    @FXML
    private GridPane buyCbs;
    @FXML
    private CheckBox cbDontSell;
    @FXML
    private CheckBox cbDontBuy;
    @FXML
    private CheckBox cbSkipIllegal;

    private VendorFilter filter;
    private Dialog<VendorFilter> dlg;

    @FXML
    private void initialize(){
        init();
    }

    void init(){
        MarketModel market = MainController.getMarket();
        sellCbs.getChildren().clear();
        buyCbs.getChildren().clear();
        initCheckboxes(market.itemsProperty().filtered(ItemModel::isMarketItem));

    }

    private void initCheckboxes(Collection<ItemModel> items){
        int column = -1;
        int row = -1;
        GroupModel currentGroup = null;
        for (ItemModel item : items) {
            row++;
            if (column == -1 || !Objects.equals(currentGroup, item.getGroup())){
                column++;
                row = 0;
                currentGroup = item.getGroup();
            }
            CheckBox cb = new CheckBox(item.getName());
            cb.setUserData(item);
            sellCbs.add(cb, column, row);
            cb = new CheckBox(item.getName());
            cb.setUserData(item);
            buyCbs.add(cb, column, row);
        }

    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("filter.title"));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(Dialogs.SAVE, Dialogs.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == Dialogs.SAVE) {
                save();
                return this.filter;
            }
            return null;
        });
        dlg.setResizable(false);
    }

    private void fill(VendorFilter filter){
        this.filter = filter;
        cbSkipIllegal.setSelected(filter.isSkipIllegal());
        cbDontSell.setSelected(filter.isDontSell());
        cbDontBuy.setSelected(filter.isDontBuy());
        fillCheckboxes(sellCbs, filter.getSellExcludes());
        fillCheckboxes(buyCbs, filter.getBuyExcludes());
    }

    private void fillCheckboxes(Pane checkboxes, Collection<Item> excludes) {
        checkboxes.getChildren().stream().filter(node -> node instanceof CheckBox).forEach(node -> {
            CheckBox checkbox = (CheckBox) node;
            ItemModel item = (ItemModel) checkbox.getUserData();
            if (item != null) {
                checkbox.setSelected(excludes.contains(ModelFabric.get(item)));
            }
        });
    }

    private void clear(){
        this.filter = null;
    }

    private void save() {
        LOG.trace("Old filter", filter);
        filter.setSkipIllegal(cbSkipIllegal.isSelected());
        filter.dontSell(cbDontSell.isSelected());
        filter.dontBuy(cbDontBuy.isSelected());
        filter.clearSellExcludes();
        sellCbs.getChildren().stream().filter(node -> node instanceof CheckBox).forEach(node -> {
            CheckBox checkbox = (CheckBox) node;
            ItemModel item = (ItemModel) checkbox.getUserData();
            if (item != null) {
                if (checkbox.isSelected()) filter.addSellExclude(ModelFabric.get(item));
            }
        });
        filter.clearBuyExcludes();
        buyCbs.getChildren().stream().filter(node -> node instanceof CheckBox).forEach(node -> {
            CheckBox checkbox = (CheckBox) node;
            ItemModel item = (ItemModel) checkbox.getUserData();
            if (item != null) {
                if (checkbox.isSelected()) filter.addBuyExclude(ModelFabric.get(item));
            }
        });
        LOG.trace("New filter", filter);
    }

    public Optional<VendorFilter> showDialog(Parent parent, Parent content){
        return showDialog(parent, content, new VendorFilter());
    }

    public boolean showEditDialog(Parent parent, Parent content, VendorFilter filter){
        return showDialog(parent, content, filter).isPresent();
    }

    private Optional<VendorFilter> showDialog(Parent parent, Parent content, VendorFilter filter){
        if (dlg == null){
            createDialog(parent, content);
        }
        fill(filter);
        Optional<VendorFilter> result = dlg.showAndWait();
        clear();
        return result;
    }

}

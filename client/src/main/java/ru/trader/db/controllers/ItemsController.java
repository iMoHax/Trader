package ru.trader.db.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import ru.trader.controllers.MainController;
import ru.trader.controllers.Screeners;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.model.GroupModel;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.FactionStringConverter;
import ru.trader.view.support.GovernmentStringConverter;
import ru.trader.view.support.Localization;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.CheckComboBoxTableCell;

import java.util.Collection;
import java.util.Optional;

public class ItemsController {

    @FXML
    private TableView<ItemModel> tblItems;
    @FXML
    private TableColumn<ItemModel, GroupModel> group;
    @FXML
    private TableColumn<ItemModel, Collection<FACTION>> factions;
    @FXML
    private TableColumn<ItemModel, Collection<GOVERNMENT>> governments;

    private ObservableList<ItemModel> items = FXCollections.observableArrayList();
    private ObservableList<GroupModel> groups = FXCollections.observableArrayList();
    private MarketModel world = null;

    @FXML
    private void initialize() {
        tblItems.setItems(items);
        group.setCellFactory(ComboBoxTableCell.forTableColumn(groups));
        factions.setCellFactory(CheckComboBoxTableCell.forTableColumn(factions,
                FXCollections.observableArrayList(FACTION.values()), new FactionStringConverter(), ItemModel::setIllegal)
        );
        governments.setCellFactory(CheckComboBoxTableCell.forTableColumn(governments,
                FXCollections.observableArrayList(GOVERNMENT.values()), new GovernmentStringConverter(), ItemModel::setIllegal)
        );
        init();
    }

    void init(){
        if (world != null) world.getNotificator().remove(marketChangeListener);
        world = MainController.getWorld();
        world.getNotificator().add(marketChangeListener);
        items.clear();
        items.addAll(world.itemsProperty());
        groups.clear();
        groups.addAll(world.getGroups());
    }

    @FXML
    private void add(){
        Screeners.showAddItem(world);
    }

    @FXML
    private void remove(){
        ItemModel item = tblItems.getSelectionModel().getSelectedItem();
        if (item != null){
            remove(item);
        }
    }

    private void remove(ItemModel item){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), item.getName()));
        if (res.isPresent() && res.get() == ButtonType.YES) {
            world.remove(item);
        }
    }


    private final ChangeMarketListener marketChangeListener = new ChangeMarketListener() {
        @Override
        public void add(ItemModel item) {
            ViewUtils.doFX(() -> {
                ItemsController.this.items.add(item);
            });
        }

        @Override
        public void remove(ItemModel item) {
            ViewUtils.doFX(() -> {
                ItemsController.this.items.remove(item);
            });
        }
    };
}

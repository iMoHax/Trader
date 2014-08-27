package ru.trader.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.Main;
import ru.trader.World;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;
import ru.trader.view.support.Localization;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController {
    private final static Logger LOG = LoggerFactory.getLogger(MainController.class);

    private static MarketModel world = new MarketModel(World.getMarket());
    private static MarketModel market = world;


    @FXML
    private BorderPane mainPane;

    @FXML
    private Menu langs;

    @FXML
    private OffersController offersController;
    @FXML
    private ItemsController itemsController;



    @FXML
    private void initialize() {
        fillLangs();
    }

    private void fillLangs() {
        ToggleGroup toggleGroup = new ToggleGroup();
        for (Locale locale : Localization.getLocales()) {
            ResourceBundle rb = Localization.getResources(locale);
            RadioMenuItem mi = new RadioMenuItem(rb.getString("main.menu.file.language.item"));
            mi.setToggleGroup(toggleGroup);
            mi.setUserData(locale);
            if (locale.equals(Localization.getCurrentLocale())) mi.setSelected(true);
            langs.getItems().add(mi);
        }
        toggleGroup.selectedToggleProperty().addListener((cb, o, n) -> {
            try {
                if (n!=null)
                    Main.changeLocale((Locale) n.getUserData());
            } catch (IOException e) {
                LOG.error("Error on change locale to {}", n.getUserData());
                LOG.error("",e);
            }
        });
    }


    public OffersController getOffersController() {
        return offersController;
    }

    public BorderPane getMainPane(){
        return mainPane;
    }

    public static MarketModel getMarket() {
        return market;
    }
    public static MarketModel getWorld() {
        return world;
    }

    public void setMarket(MarketModel market) {
        MarketModel old = MainController.market;
        MainController.market = market;
        MainController.market.addAllListener(old.getListeners());
    }

    public void save(ActionEvent actionEvent) {
        try {
            World.save();
        } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
            LOG.error("Error on save file",e);
        }
    }

    public void importWorld(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showOpenDialog(null);
            if (file !=null) {
                World.imp(file);
                reload();
            }
        } catch (SAXException | IOException e) {
            LOG.error("Error on import file", e);
        }
    }


    public Optional<ItemModel> addItem(){
        Optional<String> res = Dialogs.create()
                .title(Localization.getString("dialogs.addItem.title"))
                .message(Localization.getString("dialogs.addItem.message"))
                .showTextInput();
        ItemModel item = null;
        if (res.isPresent()){
            item = market.newItem(res.get());
            market.add(item);
        }
        return Optional.ofNullable(item);
    }


    public void addVendor(ActionEvent actionEvent) {
        Screeners.showAddVendor();
    }

    public void editVendor(ActionEvent actionEvent) {
        Screeners.showEditVendor(offersController.getVendor());
    }

    private void reload(){
        world = new MarketModel(World.getMarket());
        market = world;
        itemsController.init();
        offersController.init();
    }
}

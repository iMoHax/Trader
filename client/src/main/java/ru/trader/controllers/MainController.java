package ru.trader.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.World;
import ru.trader.model.MarketModel;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class MainController {
    private final static Logger LOG = LoggerFactory.getLogger(MainController.class);

    private static MarketModel world = new MarketModel(World.getMarket());
    private static MarketModel market = world;


    @FXML
    private BorderPane mainPane;

    @FXML
    private OffersController offersController;
    @FXML
    private ItemsController itemsController;

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


    public void addItem(ActionEvent actionEvent){
        Optional<String> res = Dialogs.create()
                .title("Добавление нового товара")
                .message("Введите название товара")
                .showTextInput();
        if (res.isPresent()) market.add(market.newItem(res.get()));
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

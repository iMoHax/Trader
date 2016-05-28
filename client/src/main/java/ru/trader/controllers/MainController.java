package ru.trader.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.Main;
import ru.trader.ServicesManager;
import ru.trader.World;
import ru.trader.model.*;
import ru.trader.services.MaddavoParserTask;
import ru.trader.view.support.Localization;
import ru.trader.view.support.ViewUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MainController {
    private final static Logger LOG = LoggerFactory.getLogger(MainController.class);

    private static MarketModel world = new MarketModel(World.getMarket());
    private static ProfileModel profile = world.getModeler().get(Main.SETTINGS.getProfile());
    private static MarketModel market = world;

    @FXML
    private BorderPane mainPane;

    @FXML
    private Menu langs;

    @FXML
    private ProfileController profController;
    @FXML
    private OffersController offersController;
    @FXML
    private ItemsController itemsController;
    @FXML
    private RouteSearchController routesController;
    @FXML
    private SearchController searchController;
    @FXML
    private RouteTrackController routeController;
    @FXML
    private TabPane tabs;
    @FXML
    private Tab track;

    @FXML
    private void initialize() {
        fillLangs();
        profController.setProfile(profile);
    }

    private void fillLangs() {
        ToggleGroup toggleGroup = new ToggleGroup();
        for (Locale locale : Localization.getLocales()) {
            ResourceBundle rb = Localization.getResources(locale);
            RadioMenuItem mi = new RadioMenuItem(rb.getString("main.menu.settings.language.item"));
            mi.setToggleGroup(toggleGroup);
            mi.setUserData(locale);
            if (locale.equals(Localization.getCurrentLocale())) mi.setSelected(true);
            langs.getItems().add(mi);
        }
        toggleGroup.selectedToggleProperty().addListener((cb, o, n) -> {
            try {
                if (n != null) {
                    Main.changeLocale((Locale) n.getUserData());
                }
            } catch (IOException e) {
                LOG.error("Error on change locale to {}", n.getUserData());
                LOG.error("", e);
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

    public static ProfileModel getProfile() {
        return profile;
    }

    public void setMarket(MarketModel market) {
        market.getNotificator().clear();
        MainController.market = market;
        Screeners.reinitAll();
    }

    void init(){
        profController.init();
        itemsController.init();
        offersController.init();
        routesController.init();
        searchController.init();
        routeController.init();
        //TODO: add init all controllers
    }

    public void initEDCE(){
        profController.initEDCEBtn();
    }

    @FXML
    private void importEDCE() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("."));
        File file = fileChooser.showOpenDialog(null);
        if (file !=null) {
            ServicesManager.getEdce().parseFile(file);
        }
    }

    public void save() {
        try {
            World.save();
        } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
            LOG.error("Error on save file",e);
        }
    }

    public void importWorld() {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML bd files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showOpenDialog(null);
            if (file !=null) {
                World.impXml(file);
                reload();
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.error("Error on import file", e);
        }
    }

    public void exportWorld() {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML bd files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showSaveDialog(null);
            if (file !=null) {
                World.saveTo(file);
                reload();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
            LOG.error("Error on save as file", e);
        }
    }

    public void clear(){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.all")));
        if (res.isPresent() && res.get() == Dialogs.OK) {
            market.clear();
            reload();
        }
    }

    public void clearOffers(){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.offers")));
        if (res.isPresent() && res.get() == Dialogs.YES) {
            market.clearOffers();
            reload();
        }
    }

    public void clearStations(){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.stations")));
        if (res.isPresent() && res.get() == Dialogs.YES) {
            market.clearStations();
            reload();
        }
    }

    public void clearSystems(){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.systems")));
        if (res.isPresent() && res.get() == Dialogs.YES) {
            market.clearSystems();
            reload();
        }
    }

    public void clearItems(){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.items")));
        if (res.isPresent() && res.get() == Dialogs.YES) {
            market.clearItems();
            reload();
        }
    }

    public void clearGroups(){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.groups")));
        if (res.isPresent() && res.get() == Dialogs.YES) {
            market.clearGroups();
            reload();
        }
    }

    public Optional<GroupModel> addGroup(){
        return Screeners.showAddGroup(market);
    }

    public Optional<ItemModel> addItem(){
        return Screeners.showAddItem(market);
    }

    public void addSystem(){
        Screeners.showSystemsEditor(null);
    }

    public void editSystem(){
        SystemModel system = profile.getSystem();
        if (!ModelFabric.isFake(system)) {
            Screeners.showSystemsEditor(system);
        }
    }

    public void removeSystem(){
        SystemModel system = profile.getSystem();
        if (!ModelFabric.isFake(system)) {
            Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), system.getName()));
            if (res.isPresent() && res.get() == Dialogs.YES) {
                market.remove(system);
            }
        }
    }

    public void addStation() {
        SystemModel system = profile.getSystem();
        if (!ModelFabric.isFake(system)) {
            Screeners.showAddStation(profile.getSystem());
        }
    }

    public void editStation() {
        StationModel station = profile.getStation();
        if (!ModelFabric.isFake(station)) {
            Screeners.showEditStation(station);
        }
    }

    public void removeStation(){
        StationModel station = profile.getStation();
        if (!ModelFabric.isFake(station)) {
            Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), station.getName()));
            if (res.isPresent() && res.get() == Dialogs.YES) {
                station.getSystem().remove(station);
            }
        }
    }

    public void editSettings(){
        Screeners.showSettings();
    }

    public void editFilter(){
        if (Screeners.showFilter(market.getAnalyzer().getFilter())){
            Main.SETTINGS.setFilter(market.getAnalyzer().getFilter());
        }
    }

    public void impMadSystems() {
        chooseFile(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"), file -> {
            MaddavoParserTask task = new MaddavoParserTask(file, MaddavoParserTask.FILE_TYPE.SYSTEMS, World.getMarket());
            Screeners.showProgress(Localization.getString("message.import.systems"), task, () -> ViewUtils.doFX(this::reload));
        });
    }

    public void impMadStations() {
        chooseFile(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"), file -> {
            MaddavoParserTask task = new MaddavoParserTask(file, MaddavoParserTask.FILE_TYPE.STATIONS, World.getMarket());
            Screeners.showProgress(Localization.getString("message.import.stations"), task, () -> ViewUtils.doFX(this::reload));
        });
    }

    public void impMadOffers() {
        chooseFile(new FileChooser.ExtensionFilter("Prices files (*.prices)", "*.prices"), file -> {
            MaddavoParserTask task = new MaddavoParserTask(file, MaddavoParserTask.FILE_TYPE.PRICES, World.getMarket());
            Screeners.showProgress(Localization.getString("message.import.prices"), task, () -> ViewUtils.doFX(this::reload));
        });
    }

    private void chooseFile(FileChooser.ExtensionFilter filter, Consumer<File> action) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(new File("."));
        File file = fileChooser.showOpenDialog(null);
        if (file !=null) {
            action.accept(file);
        }
    }

    private void reload(){
        if (world != null) world.getModeler().clear();
        world = new MarketModel(World.getMarket());
        market = world;
        profile = world.getModeler().get(ModelFabric.get(profile));
        Screeners.reinitAll();
    }

    public void showTrack(){
        tabs.getSelectionModel().select(track);
    }

    public void showDBEditor(){
        Screeners.showDBEditor();
    }
}

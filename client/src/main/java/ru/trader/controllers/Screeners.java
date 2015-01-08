package ru.trader.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import ru.trader.EMDNUpdater;
import ru.trader.Main;
import ru.trader.core.MarketFilter;
import ru.trader.model.*;
import ru.trader.view.support.CustomBuilderFactory;
import ru.trader.view.support.Localization;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;

public class Screeners {

    private static Parent mainScreen;
    private static Parent itemDescScreen;
    private static Parent vEditorScreen;
    private static Parent editOffersScreen;
    private static Parent ordersScreen;
    private static Parent topOrdersScreen;
    private static Parent pathsScreen;
    private static Parent settingsScreen;
    private static Parent sEditorScreen;
    private static Parent filterScreen;
    private static Parent itemAddScreen;
    private static Parent groupAddScreen;

    private static MainController mainController;
    private static ItemDescController itemDescController;
    private static StationEditorController vEditorController;
    private static OffersEditorController oEditorController;
    private static OrdersController ordersController;
    private static TopOrdersController topOrdersController;
    private static PathsController pathsController;
    private static SettingsController settingsController;
    private static SystemsEditorController systemsEditorController;
    private static FilterController filterController;
    private static ItemAddController itemAddController;
    private static GroupAddController groupAddController;

    private static FXMLLoader initLoader(URL url){
        FXMLLoader loader = new FXMLLoader(url, Localization.getResources());
        loader.setBuilderFactory(new CustomBuilderFactory());
        return loader;
    }

    public static Parent newScreeners(URL main, String stylesheet) throws IOException {
        FXMLLoader loader = initLoader(main);
        mainScreen = loader.load();
        if (stylesheet!=null)
            mainScreen.getStylesheets().add(stylesheet);
        mainController = loader.getController();
        return mainScreen;
    }

    private static void addStylesheet(Parent screen){
        screen.getStylesheets().addAll(mainScreen.getStylesheets());
    }

    public static void loadItemDescStage(URL fxml) throws IOException {
        FXMLLoader loader = initLoader(fxml);
        itemDescScreen = loader.load();
        addStylesheet(itemDescScreen);
        itemDescController = loader.getController();
    }

    public static void loadVEditorStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        vEditorScreen = loader.load();
        addStylesheet(vEditorScreen);
        vEditorController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(vEditorScreen));
    }

    public static void loadAddOfferStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        editOffersScreen = loader.load();
        addStylesheet(editOffersScreen);
        oEditorController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(editOffersScreen));
    }

    public static void loadOrdersStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        ordersScreen = loader.load();
        addStylesheet(ordersScreen);
        ordersController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(ordersScreen));
    }

    public static void loadTopOrdersStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        topOrdersScreen = loader.load();
        addStylesheet(topOrdersScreen);
        topOrdersController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(topOrdersScreen));
    }

    public static void loadPathsStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        pathsScreen = loader.load();
        addStylesheet(pathsScreen);
        pathsController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(pathsScreen));
    }

    public static void loadSettingsStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        settingsScreen = loader.load();
        addStylesheet(settingsScreen);
        settingsController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(settingsScreen));
    }

    public static void loadSEditorStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        sEditorScreen = loader.load();
        addStylesheet(sEditorScreen);
        systemsEditorController = loader.getController();
    }

    public static void loadFilterStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        filterScreen = loader.load();
        addStylesheet(filterScreen);
        filterController = loader.getController();
    }

    public static void loadItemAddStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        itemAddScreen = loader.load();
        addStylesheet(itemAddScreen);
        itemAddController = loader.getController();
    }

    public static void loadGroupAddStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        groupAddScreen = loader.load();
        addStylesheet(groupAddScreen);
        groupAddController = loader.getController();
    }

    public static void show(Node node){
        mainController.getMainPane().setCenter(node);
    }

    public static void showException(Throwable e){
        if (mainScreen!=null)
            Dialogs.create().owner(mainScreen).showException(e);
    }

    public static Action showConfirm(String text){
        return Dialogs.create().owner(mainScreen).message(text).showConfirm();
    }

    public static GroupModel showAddGroup(MarketModel market){
        return groupAddController.showDialog(mainScreen, groupAddScreen, market);
    }

    public static ItemModel showAddItem(MarketModel market){
        return itemAddController.showDialog(mainScreen, itemAddScreen, market);
    }

    public static void showSystemsEditor(SystemModel system){
        systemsEditorController.showDialog(mainScreen, sEditorScreen, system);
    }

    public static void showAddStation(SystemModel system){
        vEditorController.showDialog(mainScreen, vEditorScreen, system, null);
    }

    public static void showEditStation(StationModel station){
        vEditorController.showDialog(mainScreen, vEditorScreen, station);
    }

    public static Parent getMainScreen(){
        return mainScreen;
    }

    public static Optional<OffersEditorController.DialogResult> showEditOffers(ItemModel item, Number sell, Number buy) {
        return oEditorController.showDialog(vEditorScreen, editOffersScreen, item, sell, buy);
    }


    public static Collection<OrderModel> showOrders(Collection<OfferModel> offers, double balance, long cargo) {
        return ordersController.showDialog(mainScreen, ordersScreen, offers, balance, cargo);
    }

    public static void changeItemDesc(ItemModel item){
        itemDescController.setItemDesc(item);
    }

    public static void showItemDesc(Node owner){
        itemDescController.popup(owner, itemDescScreen);

    }

    public static Optional<ItemModel> showAddItem(){
        return mainController.addItem();
    }
    public static Optional<GroupModel> showAddGroup(){
        return mainController.addGroup();
    }

    public static void closeAll() {
        itemDescController.close();
    }

    public static OrderModel showOrders(ObservableList<OrderModel> orders) {
        return topOrdersController.showDialog(mainScreen, topOrdersScreen, orders);
    }

    public static PathRouteModel showRouters(ObservableList<PathRouteModel> routers) {
        return pathsController.showDialog(mainScreen, pathsScreen, routers);
    }

    public static void showSettings() {
        settingsController.showDialog(mainScreen, settingsScreen);
    }

    public static MarketFilter showFilter() {
        Action res = filterController.showDialog(mainScreen, filterScreen);
        return res == filterController.actSave ? filterController.getFilter() : null;
    }

    public static void showFilter(MarketFilter filter) {
        Action res = filterController.showDialog(mainScreen, filterScreen, filter);
        if (res == filterController.actSave){
            Main.SETTINGS.setFilter(filter);
        }
    }

    public static void reinitAll() {
        mainController.init();
        filterController.init();
        EMDNUpdater.setMarket(MainController.getMarket());
    }
}

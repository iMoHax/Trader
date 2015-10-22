package ru.trader.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import ru.trader.EMDNUpdater;
import ru.trader.core.MarketFilter;
import ru.trader.model.*;
import ru.trader.view.support.CustomBuilderFactory;
import ru.trader.view.support.Localization;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class Screeners {

    private static Parent mainScreen;
    private static Parent itemDescScreen;
    private static Parent vEditorScreen;
    private static Parent topOrdersScreen;
    private static Parent pathsScreen;
    private static Parent settingsScreen;
    private static Parent sEditorScreen;
    private static Parent filterScreen;
    private static Parent itemAddScreen;
    private static Parent groupAddScreen;
    private static Parent loginScreen;
    private static Parent helperScreen;

    private static MainController mainController;
    private static ItemDescController itemDescController;
    private static StationEditorController vEditorController;
    private static TopOrdersController topOrdersController;
    private static PathsController pathsController;
    private static SettingsController settingsController;
    private static SystemsEditorController systemsEditorController;
    private static FilterController filterController;
    private static ItemAddController itemAddController;
    private static GroupAddController groupAddController;
    private static LoginController loginController;
    private static HelperController helperController;

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

    private static void addStylesheet(Parent screen) {
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

    public static void loadLoginStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        loginScreen = loader.load();
        addStylesheet(loginScreen);
        loginController = loader.getController();
    }

    public static void loadHelperStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        helperScreen = loader.load();
        addStylesheet(helperScreen);
        helperController = loader.getController();
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

    public static Optional<GroupModel> showAddGroup(MarketModel market){
        return groupAddController.showDialog(mainScreen, groupAddScreen, market);
    }

    public static Optional<ItemModel> showAddItem(MarketModel market){
        return itemAddController.showDialog(mainScreen, itemAddScreen, market);
    }

    public static void showSystemsEditor(SystemModel system){
        systemsEditorController.showDialog(mainScreen, sEditorScreen, system);
    }

    public static void showAddStation(SystemModel system){
        vEditorController.showDialog(mainScreen, vEditorScreen, system);
    }

    public static void showEditStation(StationModel station){
        vEditorController.showDialog(mainScreen, vEditorScreen, station);
    }

    public static Parent getMainScreen(){
        return mainScreen;
    }

    public static MainController getMainController(){return mainController;}

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
        helperController.close();
    }

    public static Optional<OrderModel> showOrders(ObservableList<OrderModel> orders) {
        return topOrdersController.showDialog(mainScreen, topOrdersScreen, orders);
    }

    public static Optional<RouteModel> showRouters(ObservableList<RouteModel> routers) {
        return pathsController.showDialog(mainScreen, pathsScreen, routers);
    }

    public static void showSettings() {
        settingsController.showDialog(mainScreen, settingsScreen);
    }

    public static Optional<MarketFilter> showFilter() {
        return filterController.showDialog(mainScreen, filterScreen);
    }

    public static boolean showFilter(MarketFilter filter) {
        return filterController.showEditDialog(mainScreen, filterScreen, filter);
    }

    public static Optional<Pair<String, String>> showLogin() {
        return loginController.showDialog(mainScreen, loginScreen);
    }

    public static Optional<String> showVerifyCodeDialog(){
        return showTextDialog(Localization.getString("verify.title"),
                Localization.getString("verify.header"),
                Localization.getString("verify.content")
        );
    }

    public static Optional<String> showTextDialog(String title, String header, String content){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }

    public static void showHelper(){
        helperController.show(helperScreen, false);
    }

    public static void toggleHelper(){
        helperController.show(helperScreen, true);
    }

    public static void reinitAll() {
        mainController.init();
        systemsEditorController.init();
        vEditorController.init();
        filterController.init();
        EMDNUpdater.setMarket(MainController.getMarket());
    }
}

package ru.trader.controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Pair;
import ru.trader.EMDNUpdater;
import ru.trader.core.Engine;
import ru.trader.core.MarketFilter;
import ru.trader.core.VendorFilter;
import ru.trader.db.controllers.DBEditorController;
import ru.trader.model.*;
import ru.trader.view.support.CustomBuilderFactory;
import ru.trader.view.support.Localization;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

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
    private static Parent vFilterScreen;
    private static Parent dbEditorScreen;
    private static Parent engineModScreen;

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
    private static VendorFilterController vFilterController;
    private static DBEditorController dbEditorController;
    private static EngineModController engineModController;

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

    public static void loadVendorFilterStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        vFilterScreen = loader.load();
        addStylesheet(vFilterScreen);
        vFilterController = loader.getController();
    }

    public static void loadDBEditorStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        dbEditorScreen = loader.load();
        addStylesheet(dbEditorScreen);
        dbEditorController = loader.getController();
    }

    public static void loadEngineModStage(URL fxml) throws IOException {
        FXMLLoader loader =  initLoader(fxml);
        engineModScreen = loader.load();
        addStylesheet(engineModScreen);
        engineModController = loader.getController();
    }

    public static void show(Node node){
        mainController.getMainPane().setCenter(node);
    }

    public static void showException(Throwable ex){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(Localization.getString("dialog.exception.title","Exception Dialog"));
        String text = ex.getLocalizedMessage();
        alert.setHeaderText(text != null ? text : ex.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label(Localization.getString("dialog.exception.label.stacktrace","The exception stacktrace was:"));
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirm(String text){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Localization.getString("dialog.confirm.title"));
        alert.setHeaderText(text);
        alert.getButtonTypes().setAll(Dialogs.YES, Dialogs.NO, Dialogs.CANCEL);
        return alert.showAndWait();
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
        dbEditorController.close();
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

    public static void showInfo(String title, String header, String message){
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    public static void showProgress(String title, Task<Void> task, Runnable onSuccess){
        showProgress(title, task, (t) -> onSuccess.run(), true);
    }

    public static <T> void showProgress(String title, Task<T> task, Consumer<T> onSuccess, boolean showFinishInfo){
        ProgressController progress = new ProgressController(mainScreen, title);
        progress.run(task, t -> {
            if (showFinishInfo) showInfo(title, Localization.getString("message.finish"), null);
            onSuccess.accept(t);
        });
    }

    public static void showHelper(){
        helperController.show(helperScreen, false);
    }

    public static void toggleHelper(){
        helperController.show(helperScreen, true);
    }

    public static void showDBEditor(){
        dbEditorController.show(dbEditorScreen, false);
    }

    public static void showTrackTab(){
        mainController.showTrack();
    }

    public static Optional<VendorFilter> showVendorFilter() {
        return vFilterController.showDialog(mainScreen, vFilterScreen);
    }

    public static boolean showFilter(VendorFilter filter) {
        return vFilterController.showEditDialog(mainScreen, vFilterScreen, filter);
    }

    public static Optional<Engine> showModEngine(){
        return engineModController.showDialog(mainScreen, engineModScreen);
    }

    public static void reinitAll() {
        mainController.init();
        systemsEditorController.init();
        vEditorController.init();
        filterController.init();
        vFilterController.init();
        dbEditorController.init();
        EMDNUpdater.setMarket(MainController.getMarket());
    }

}

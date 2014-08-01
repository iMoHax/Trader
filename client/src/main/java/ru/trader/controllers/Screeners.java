package ru.trader.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import ru.trader.model.*;
import ru.trader.view.support.CustomBuilderFactory;

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

    private static MainController mainController;
    private static ItemDescController itemDescController;
    private static VendorEditorController vEditorController;
    private static OffersEditorController oEditorController;
    private static OrdersController ordersController;

    private static FXMLLoader initLoader(URL url){
        FXMLLoader loader = new FXMLLoader(url);
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

    public static Action showAddVendor(){
        return vEditorController.showDialog(mainScreen, vEditorScreen, null);
    }

    public static Action showEditVendor(VendorModel vendor){
        return vEditorController.showDialog(mainScreen, vEditorScreen, vendor);
    }

    public static Parent getMainScreen(){
        return mainScreen;
    }

    public static Optional<OffersEditorController.DialogResult> showEditOffers(ItemModel item, Number sell, Number buy) {
        return oEditorController.showDialog(vEditorScreen, editOffersScreen, item, sell, buy);
    }


    public static Collection<OrderModel> showOrders(Collection<OfferDescModel> offers, double balance, long cargo) {
        return ordersController.showDialog(mainScreen, ordersScreen, offers, balance, cargo);
    }

    public static void changeItemDesc(ItemDescModel item){
        itemDescController.setItemDesc(item);
    }

    public static void showItemDesc(Node owner){
        itemDescController.popup(owner, itemDescScreen);

    }

    public static Optional<ItemModel> showAddItem(){
        return mainController.addItem();
    }

    public static void closeAll() {
        itemDescController.close();
    }
}

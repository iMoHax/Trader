package ru.trader.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import ru.trader.model.*;

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

    public static Parent newScreeners(URL main, String stylesheet) throws IOException {
        FXMLLoader loader = new FXMLLoader(main);
        mainScreen = loader.load();
        if (stylesheet!=null)
            mainScreen.getStylesheets().add(stylesheet);
        mainController = loader.getController();
        return mainScreen;
    }


    public static void loadItemDescStage(URL fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(fxml);
        itemDescScreen = loader.load();
        itemDescController = loader.getController();
    }

    public static void loadVEditorStage(URL fxml) throws IOException {
        FXMLLoader loader =  new FXMLLoader(fxml);
        vEditorScreen = loader.load();
        vEditorController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(vEditorScreen));
    }

    public static void loadAddOfferStage(URL fxml) throws IOException {
        FXMLLoader loader =  new FXMLLoader(fxml);
        editOffersScreen = loader.load();
        oEditorController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(editOffersScreen));
    }

    public static void loadOrdersStage(URL fxml) throws IOException {
        FXMLLoader loader =  new FXMLLoader(fxml);
        ordersScreen = loader.load();
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

    public static void closeAll() {
        itemDescController.close();
    }
}

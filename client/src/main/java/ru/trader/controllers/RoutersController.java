package ru.trader.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import ru.trader.model.MarketModel;
import ru.trader.model.OfferDescModel;
import ru.trader.model.OrderModel;
import ru.trader.model.VendorModel;
import ru.trader.view.support.NumberField;


import java.util.Collection;


public class RoutersController {

    @FXML
    private NumberField balance;
    @FXML
    private NumberField cargo;
    @FXML
    private NumberField distance;
    @FXML
    private NumberField tank;
    @FXML
    private NumberField jumps;

    @FXML
    private Button add;

    @FXML
    private ComboBox<VendorModel> source;
    @FXML
    private ComboBox<VendorModel> target;

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private NumberField totalProfit;

    @FXML
    private NumberField totalBalance;

    private MarketModel market;

    @FXML
    private void initialize(){
        init();
        balance.numberProperty().addListener((ov, o, n) -> totalBalance.setValue(n));
        cargo.numberProperty().addListener((ov, o, n) -> market.setCargo(n.longValue()));
        tank.numberProperty().addListener((ov, o, n) -> market.setTank(n.doubleValue()));
        distance.numberProperty().addListener((ov, o, n) -> market.setDistance(n.doubleValue()));
        jumps.numberProperty().addListener((ov, o, n) -> market.setJumps(n.intValue()));

        balance.setValue(1000);
        cargo.setValue(4);
        tank.setValue(30);
        distance.setValue(7);
        jumps.setValue(4);

        add.disableProperty().bind(this.balance.wrongProperty().or(this.cargo.wrongProperty()));
        tblOrders.setItems(FXCollections.observableArrayList());
        tblOrders.getItems().addListener((ListChangeListener<OrderModel>) c -> {
            while (c.next()) {
                if (c.wasRemoved()){
                    for (OrderModel o : c.getRemoved()) {
                        onRemove(o);
                    }
                }
                if (c.wasAdded()){
                    for (OrderModel o : c.getAddedSubList()) {
                       onAdd(o);
                    }
                }
            }
        });
    }


    void init(){
        market = MainController.getMarket();
        source.setItems(market.vendorsProperty());
        source.getSelectionModel().selectFirst();
        target.setItems(market.vendorsProperty());
        tblOrders.getItems().clear();
        totalBalance.setValue(balance.getValue());
        totalProfit.setValue(0);
    }

    private Collection<OfferDescModel> getOffers(){
        VendorModel vendor = source.getSelectionModel().getSelectedItem();
        return vendor.getSells(market::asOfferDescModel);
    }

    private void onAdd(OrderModel order){
        totalProfit.add(order.getProfit());
        totalBalance.add(order.getProfit());
        source.getSelectionModel().select(order.getBuyer().getVendor());
    }

    private void onRemove(OrderModel order) {
        totalProfit.sub(order.getProfit());
        totalBalance.sub(order.getProfit());
        source.getSelectionModel().select(order.getVendor());
    }

    public void addOrders(){
        Collection<OrderModel> orders = Screeners.showOrders(getOffers(), totalBalance.getValue().doubleValue(), cargo.getValue().longValue());
        if (orders!=null){
            tblOrders.getItems().addAll(orders);
        }
    }

    public void removeSelected(){
        TableView.TableViewSelectionModel<OrderModel> select = tblOrders.getSelectionModel();
        if (!select.isEmpty()){
            int index = select.getSelectedIndex();
            tblOrders.getItems().remove(index);
        }
    }

    public void removeAll(){
        tblOrders.getItems().clear();
        totalBalance.setValue(balance.getValue());
        totalProfit.setValue(0);
    }


    public void showTopOrders(){
        OrderModel order = Screeners.showTopOrders(market.getTop(100, totalBalance.getValue().doubleValue()));
        if (order!=null){
            tblOrders.getItems().add(order);
        }
    }

    public void showRoutes(){
        VendorModel s = source.getSelectionModel().getSelectedItem();
        VendorModel t = target.getSelectionModel().getSelectedItem();
        if (t==null) return;

        MarketModel market = MainController.getMarket();
        Screeners.showRouters(market.getRouters(s, t, totalBalance.getValue().doubleValue()));
    }


}

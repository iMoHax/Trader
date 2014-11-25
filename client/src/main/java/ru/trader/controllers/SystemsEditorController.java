package ru.trader.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.model.MarketModel;
import ru.trader.model.SystemModel;
import ru.trader.view.support.Localization;
import ru.trader.view.support.cells.TextFieldCell;

public class SystemsEditorController {
    private final static Logger LOG = LoggerFactory.getLogger(SystemsEditorController.class);


    @FXML
    private TableView<SystemData> tblSystems;
    @FXML
    private TableColumn<SystemData, String> clnName;
    @FXML
    private TableColumn<SystemData, Double> clnX;
    @FXML
    private TableColumn<SystemData, Double> clnY;
    @FXML
    private TableColumn<SystemData, Double> clnZ;
    @FXML
    private TableColumn<SystemData, Double> clnS1;
    @FXML
    private TableColumn<SystemData, Double> clnS2;
    @FXML
    private TableColumn<SystemData, Double> clnS3;
    @FXML
    private TableColumn<SystemData, Double> clnS4;
    @FXML
    private TableColumn<SystemData, Double> clnS5;
    @FXML
    private TableColumn<SystemData, Double> clnS6;
    @FXML
    private ComboBox<SystemModel> system1;
    @FXML
    private ComboBox<SystemModel> system2;
    @FXML
    private ComboBox<SystemModel> system3;
    @FXML
    private ComboBox<SystemModel> system4;
    @FXML
    private ComboBox<SystemModel> system5;
    @FXML
    private ComboBox<SystemModel> system6;

    private MarketModel market;

    private final Action actSave = new DialogAction(Localization.getString("dialog.button.save"), ButtonBar.ButtonType.OK_DONE, false, true, false, (e) -> {
        tblSystems.getSelectionModel().selectFirst();
        commit();
    });

    @FXML
    private void initialize() {
        clnName.setCellFactory(TextFieldCell.forTableColumn(new DefaultStringConverter()));
        clnX.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnY.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnZ.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS1.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS2.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS3.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS4.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS5.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS6.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        tblSystems.setItems(FXCollections.observableArrayList());
        tblSystems.getSelectionModel().setCellSelectionEnabled(true);
        init();
    }

    private void init(){
        market = MainController.getMarket();
        system1.setItems(market.systemsProperty());
        system2.setItems(market.systemsProperty());
        system3.setItems(market.systemsProperty());
        system4.setItems(market.systemsProperty());
        system5.setItems(market.systemsProperty());
        system6.setItems(market.systemsProperty());
    }

    public void showDialog(Parent parent, Parent content, SystemModel system){
        Dialog dlg = new Dialog(parent, Localization.getString("sEditor.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(actSave,  Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        if (system != null){
            tblSystems.getItems().add(new SystemData(system));
        }
        for (int i = 0; i < 10; i++) {
            add();
        }
        dlg.show();
        reset();
    }

    public void add() {
        tblSystems.getItems().add(new SystemData());
    }


    private void commit(){
        for (SystemData systemData : tblSystems.getItems()) {
            systemData.commit();
        }
    }

    private void reset(){
        tblSystems.getItems().clear();
    }

    public class SystemData {
        private final StringProperty name;
        private final DoubleProperty x;
        private final DoubleProperty y;
        private final DoubleProperty z;

        private final DoubleProperty s1 = new SimpleDoubleProperty();
        private final DoubleProperty s2 = new SimpleDoubleProperty();
        private final DoubleProperty s3 = new SimpleDoubleProperty();
        private final DoubleProperty s4 = new SimpleDoubleProperty();
        private final DoubleProperty s5 = new SimpleDoubleProperty();
        private final DoubleProperty s6 = new SimpleDoubleProperty();
        private final SystemModel system;

        private SystemData() {
            system = null;
            name = new SimpleStringProperty("");
            x = new SimpleDoubleProperty(Double.NaN);
            y = new SimpleDoubleProperty(Double.NaN);
            z = new SimpleDoubleProperty(Double.NaN);
        }

        private SystemData(SystemModel system) {
            this.system = system;
            name = new SimpleStringProperty(system.getName());
            x = new SimpleDoubleProperty(system.getX());
            y = new SimpleDoubleProperty(system.getY());
            z = new SimpleDoubleProperty(system.getZ());
        }

        public StringProperty nameProperty() {
            return name;
        }

        public DoubleProperty xProperty() {
            return x;
        }

        public DoubleProperty yProperty() {
            return y;
        }

        public DoubleProperty zProperty() {
            return z;
        }

        public DoubleProperty s1Property() {
            return s1;
        }

        public DoubleProperty s2Property() {
            return s2;
        }

        public DoubleProperty s3Property() {
            return s3;
        }

        public DoubleProperty s4Property() {
            return s4;
        }

        public DoubleProperty s5Property() {
            return s5;
        }

        public DoubleProperty s6Property() {
            return s6;
        }

        private void commit(){
            if (!name.get().isEmpty() && !Double.isNaN(x.get()) && !Double.isNaN(y.get()) && !Double.isNaN(z.get())){
                if (system != null){
                    system.setName(name.get());
                    system.setPosition(x.get(), y.get(), z.get());
                } else {
                    market.add(name.get(), x.get(), y.get(), z.get());
                }
            }
        }

    }

}

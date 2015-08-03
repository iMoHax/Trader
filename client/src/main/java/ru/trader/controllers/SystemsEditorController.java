package ru.trader.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.model.MarketModel;
import ru.trader.model.SystemModel;
import ru.trader.model.support.PositionComputer;
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

    private Dialog<ButtonType> dlg;
    private MarketModel market;

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
        tblSystems.setSortPolicy(t -> true);
        system1.valueProperty().addListener((ov, o, n) -> clnS1.setText(n != null ? n.getName() : ""));
        system2.valueProperty().addListener((ov, o, n) -> clnS2.setText(n != null ? n.getName() : ""));
        system3.valueProperty().addListener((ov, o, n) -> clnS3.setText(n != null ? n.getName() : ""));
        system4.valueProperty().addListener((ov, o, n) -> clnS4.setText(n != null ? n.getName() : ""));
        system5.valueProperty().addListener((ov, o, n) -> clnS5.setText(n != null ? n.getName() : ""));
        system6.valueProperty().addListener((ov, o, n) -> clnS6.setText(n != null ? n.getName() : ""));
        init();
    }

    void init(){
        market = MainController.getMarket();
        system1.setItems(market.systemsProperty());
        system2.setItems(market.systemsProperty());
        system3.setItems(market.systemsProperty());
        system4.setItems(market.systemsProperty());
        system5.setItems(market.systemsProperty());
        system6.setItems(market.systemsProperty());
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("sEditor.title"));
        ButtonType saveButton = new ButtonType(Localization.getString("dialog.button.save"), ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                save();
            }
            return dialogButton;
        });
        dlg.setResizable(false);
    }

    private void save(){
        tblSystems.getSelectionModel().selectFirst();
        commit();
    }

    public void showDialog(Parent parent, Parent content, SystemModel system){
        if (dlg == null){
            createDialog(parent, content);
        }
        fill(system);
        dlg.showAndWait();
        clear();
    }

    private void fill(SystemModel system){
        if (system != null){
            tblSystems.getItems().add(new SystemData(system));
        }
        for (int i = 0; i < 10; i++) {
            add();
        }
    }

    public void add(){
        tblSystems.getItems().add(new SystemData());
    }

    public void compute(){
        SystemModel sys1 = system1.getValue();
        SystemModel sys2 = system2.getValue();
        SystemModel sys3 = system3.getValue();
        SystemModel sys4 = system4.getValue();
        SystemModel sys5 = system5.getValue();
        SystemModel sys6 = system6.getValue();

        for (SystemData systemData : tblSystems.getItems()) {
            if (systemData.name.isEmpty().get()) continue;
            PositionComputer computer = new PositionComputer();
            if (sys1 != null) computer.addLandMark(sys1, systemData.s1.get());
            if (sys2 != null) computer.addLandMark(sys2, systemData.s2.get());
            if (sys3 != null) computer.addLandMark(sys3, systemData.s3.get());
            if (sys4 != null) computer.addLandMark(sys4, systemData.s4.get());
            if (sys5 != null) computer.addLandMark(sys5, systemData.s5.get());
            if (sys6 != null) computer.addLandMark(sys6, systemData.s6.get());
            PositionComputer.Coordinates coord = computer.compute();
            systemData.x.set(coord.getX());
            systemData.y.set(coord.getY());
            systemData.z.set(coord.getZ());
        }
    }

    private void commit(){
        for (SystemData systemData : tblSystems.getItems()) {
            systemData.commit();
        }
    }

    private void clear(){
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

        public boolean isEmpty(){
            return name.get().isEmpty() || Double.isNaN(x.get()) || Double.isNaN(y.get()) || Double.isNaN(z.get());
        }

        private void commit(){
            if (!isEmpty()){
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

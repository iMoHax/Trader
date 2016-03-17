package ru.trader.controllers;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.Main;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.POWER;
import ru.trader.core.POWER_STATE;
import ru.trader.model.MarketModel;
import ru.trader.model.ModelFabric;
import ru.trader.model.ProfileModel;
import ru.trader.model.SystemModel;
import ru.trader.model.support.PositionComputer;
import ru.trader.view.support.*;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;
import ru.trader.view.support.cells.TextFieldCell;

public class SystemsEditorController {
    private final static Logger LOG = LoggerFactory.getLogger(SystemsEditorController.class);


    @FXML
    private TableView<SystemData> tblSystems;
    @FXML
    private TableColumn<SystemData, String> clnName;
    @FXML
    private TableColumn<SystemData, FACTION> clnFaction;
    @FXML
    private TableColumn<SystemData, GOVERNMENT> clnGovernment;
    @FXML
    private TableColumn<SystemData, POWER> clnPower;
    @FXML
    private TableColumn<SystemData, POWER_STATE> clnPowerState;
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
    private TextField system1Text;
    private AutoCompletion<SystemModel> system1;
    @FXML
    private TextField system2Text;
    private AutoCompletion<SystemModel> system2;
    @FXML
    private TextField system3Text;
    private AutoCompletion<SystemModel> system3;
    @FXML
    private TextField system4Text;
    private AutoCompletion<SystemModel> system4;
    @FXML
    private TextField system5Text;
    private AutoCompletion<SystemModel> system5;
    @FXML
    private TextField system6Text;
    private AutoCompletion<SystemModel> system6;

    private Dialog<ButtonType> dlg;
    private MarketModel market;

    @FXML
    private void initialize() {
        init();
        clnName.setCellFactory(TextFieldCell.forTableColumn(new DefaultStringConverter()));
        clnFaction.setCellFactory(ComboBoxTableCell.forTableColumn(new FactionStringConverter(), FXCollections.observableArrayList(FACTION.values())));
        clnGovernment.setCellFactory(ComboBoxTableCell.forTableColumn(new GovernmentStringConverter(), FXCollections.observableArrayList(GOVERNMENT.values())));
        clnPower.setCellFactory(ComboBoxTableCell.forTableColumn(new PowerStringConverter(), FXCollections.observableArrayList(POWER.values())));
        clnPowerState.setCellFactory(ComboBoxTableCell.forTableColumn(new PowerStateStringConverter(), FXCollections.observableArrayList(POWER_STATE.values())));
        clnX.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnY.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnZ.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS1.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS2.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS3.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS4.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS5.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        clnS6.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        ViewUtils.addOnEditCommit(clnName, e -> copySys1());
        ViewUtils.addOnEditCommit(clnS1, e -> copySys2());
        ViewUtils.addOnEditCommit(clnS2, e -> copySys3());
        ViewUtils.addOnEditCommit(clnS3, e -> copySys4());
        ViewUtils.addOnEditCommit(clnS4, e -> copySys5());
        ViewUtils.addOnEditCommit(clnS5, e -> copySys6());
        tblSystems.setItems(FXCollections.observableArrayList());
        tblSystems.getSelectionModel().setCellSelectionEnabled(true);
        tblSystems.setSortPolicy(t -> true);
        system1.valueProperty().addListener((ov, o, n) -> clnS1.setText(n != null ? n.getName() : ""));
        system2.valueProperty().addListener((ov, o, n) -> clnS2.setText(n != null ? n.getName() : ""));
        system3.valueProperty().addListener((ov, o, n) -> clnS3.setText(n != null ? n.getName() : ""));
        system4.valueProperty().addListener((ov, o, n) -> clnS4.setText(n != null ? n.getName() : ""));
        system5.valueProperty().addListener((ov, o, n) -> clnS5.setText(n != null ? n.getName() : ""));
        system6.valueProperty().addListener((ov, o, n) -> clnS6.setText(n != null ? n.getName() : ""));
    }

    void init(){
        market = MainController.getMarket();
        SystemsProvider provider = market.getSystemsProvider();
        if (system1 == null){
            system1 = new AutoCompletion<>(system1Text, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system1.setSuggestions(provider.getPossibleSuggestions());
            system1.setConverter(provider.getConverter());
        }
        if (system2 == null){
            system2 = new AutoCompletion<>(system2Text, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system2.setSuggestions(provider.getPossibleSuggestions());
            system2.setConverter(provider.getConverter());
        }
        if (system3 == null){
            system3 = new AutoCompletion<>(system3Text, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system3.setSuggestions(provider.getPossibleSuggestions());
            system3.setConverter(provider.getConverter());
        }
        if (system4 == null){
            system4 = new AutoCompletion<>(system4Text, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system4.setSuggestions(provider.getPossibleSuggestions());
            system4.setConverter(provider.getConverter());
        }
        if (system5 == null){
            system5 = new AutoCompletion<>(system5Text, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system5.setSuggestions(provider.getPossibleSuggestions());
            system5.setConverter(provider.getConverter());
        }
        if (system6 == null){
            system6 = new AutoCompletion<>(system6Text, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system6.setSuggestions(provider.getPossibleSuggestions());
            system6.setConverter(provider.getConverter());
        }
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
            ProfileModel profile = MainController.getProfile();
            SystemModel s = profile.getPrevSystem();
            if (!ModelFabric.isFake(s)){
                searchLandMark(s, profile.getEmptyMaxShipJumpRange()*1.5);
                copySys1();
            }
        }
        for (int i = 0; i < 10; i++) {
            add();
        }
    }

    private void searchLandMark(SystemModel system, double maxDistance){
        double x = system.getX(), y = system.getY(), z = system.getZ(), e = 6;
        system1.setValue(market.getNear(x + maxDistance, y, z, Double.POSITIVE_INFINITY, e, e));
        system2.setValue(market.getNear(x - maxDistance, y, z, Double.NEGATIVE_INFINITY, e, e));
        system3.setValue(market.getNear(x, y + maxDistance, z, e, Double.POSITIVE_INFINITY, e));
        system4.setValue(market.getNear(x, y - maxDistance, z, e, Double.NEGATIVE_INFINITY, e));
        system5.setValue(market.getNear(x, y, z + maxDistance, e, e, Double.POSITIVE_INFINITY));
        system6.setValue(market.getNear(x, y, z - maxDistance, e, e, Double.NEGATIVE_INFINITY));
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

    @FXML
    private void copySys1(){
        Main.copyToClipboard(system1Text.getText());
    }

    @FXML
    private void copySys2(){
        Main.copyToClipboard(system2Text.getText());
    }

    @FXML
    private void copySys3(){
        Main.copyToClipboard(system3Text.getText());
    }

    @FXML
    private void copySys4(){
        Main.copyToClipboard(system4Text.getText());
    }

    @FXML
    private void copySys5(){
        Main.copyToClipboard(system5Text.getText());
    }

    @FXML
    private void copySys6(){
        Main.copyToClipboard(system6Text.getText());
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
        private final ObjectProperty<FACTION> faction;
        private final ObjectProperty<GOVERNMENT> government;
        private final ObjectProperty<POWER> power;
        private final ObjectProperty<POWER_STATE> powerState;

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
            faction = new SimpleObjectProperty<>(FACTION.NONE);
            government = new SimpleObjectProperty<>(GOVERNMENT.NONE);
            power = new SimpleObjectProperty<>(POWER.NONE);
            powerState = new SimpleObjectProperty<>(POWER_STATE.NONE);
        }

        private SystemData(SystemModel system) {
            this.system = system;
            name = new SimpleStringProperty(system.getName());
            x = new SimpleDoubleProperty(system.getX());
            y = new SimpleDoubleProperty(system.getY());
            z = new SimpleDoubleProperty(system.getZ());
            faction = new SimpleObjectProperty<>(system.getFaction());
            government = new SimpleObjectProperty<>(system.getGovernment());
            power = new SimpleObjectProperty<>(system.getPower());
            powerState = new SimpleObjectProperty<>(system.getPowerState());
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

        public ObjectProperty<FACTION> factionProperty() {
            return faction;
        }

        public ObjectProperty<GOVERNMENT> governmentProperty() {
            return government;
        }

        public ObjectProperty<POWER> powerProperty() {
            return power;
        }

        public ObjectProperty<POWER_STATE> powerStateProperty() {
            return powerState;
        }

        public boolean isEmpty(){
            return name.get().isEmpty() || Double.isNaN(x.get()) || Double.isNaN(y.get()) || Double.isNaN(z.get());
        }

        private void commit(){
            if (!isEmpty()){
                if (system != null){
                    system.setName(name.get());
                    system.setPosition(x.get(), y.get(), z.get());
                    system.setFaction(faction.get());
                    system.setGovernment(government.get());
                    system.setPower(power.get());
                    system.setPowerState(powerState.get());
                } else {
                    SystemModel system = market.add(name.get(), x.get(), y.get(), z.get());
                    system.setFaction(faction.get());
                    system.setGovernment(government.get());
                    system.setPower(power.get());
                    system.setPowerState(powerState.get());
                }
            }
        }

    }

}

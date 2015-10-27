package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.Main;
import ru.trader.core.Profile;
import ru.trader.view.support.Localization;
import ru.trader.view.support.NumberField;


public class SettingsController {
    private final static Logger LOG = LoggerFactory.getLogger(SettingsController.class);

    @FXML
    private CheckBox emdnOn;
    @FXML
    private TextField emdnSubServ;
    @FXML
    private CheckBox emdnUpdateOnly;
    @FXML
    private NumberField emdnUpdateTime;

    @FXML
    private NumberField jumps;
    @FXML
    private NumberField lands;
    @FXML
    private NumberField routesCount;
    @FXML
    private NumberField fuelPrice;
    @FXML
    private ComboBox<Profile.PATH_PRIORITY> pathPriority;
    @FXML
    private NumberField jumpTime;
    @FXML
    private NumberField landingTime;
    @FXML
    private NumberField takeoffTime;
    @FXML
    private NumberField rechargeTime;

    @FXML
    private CheckBox edceActive;
    @FXML
    private NumberField edceInterval;

    private Dialog<ButtonType> dlg;

    @FXML
    private void initialize(){
        pathPriority.setItems(FXCollections.observableArrayList(Profile.PATH_PRIORITY.values()));
        init();
    }

    private void init(){
/*
        emdnSubServ.setText(Main.SETTINGS.getEMDNSub());
        emdnOn.setSelected(Main.SETTINGS.getEMDNActive());
        emdnUpdateOnly.setSelected(Main.SETTINGS.getEMDNUpdateOnly());
        emdnUpdateTime.setValue(Main.SETTINGS.getEMDNAutoUpdate());
*/

        Profile profile =Main.SETTINGS.getProfile();
        jumps.setValue(profile.getJumps());
        lands.setValue(profile.getLands());
        routesCount.setValue(profile.getRoutesCount());
        fuelPrice.setValue(profile.getFuelPrice());
        pathPriority.setValue(profile.getPathPriority());
        jumpTime.setValue(profile.getJumpTime());
        landingTime.setValue(profile.getLandingTime());
        takeoffTime.setValue(profile.getTakeoffTime());
        rechargeTime.setValue(profile.getRechargeTime());

        edceActive.setSelected(Main.SETTINGS.edce().isActive());
        edceInterval.setValue(Main.SETTINGS.edce().getInterval());
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("settings.title"));
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

    private void save() {
/*
        Main.SETTINGS.setEMDNSub(emdnSubServ.getText());
        EMDNUpdater.setSub(emdnSubServ.getText());
        Main.SETTINGS.setEMDNActive(emdnOn.isSelected());
        EMDNUpdater.setActivate(emdnOn.isSelected());
        Main.SETTINGS.setEMDNUpdateOnly(emdnUpdateOnly.isSelected());
        EMDNUpdater.setUpdateOnly(emdnUpdateOnly.isSelected());
        Main.SETTINGS.setEMDNAutoUpdate(emdnUpdateTime.getValue().longValue());
        EMDNUpdater.setInterval(emdnUpdateTime.getValue().longValue());
*/

        Profile profile =Main.SETTINGS.getProfile();
        profile.setJumps(jumps.getValue().intValue());
        profile.setLands(lands.getValue().intValue());
        profile.setRoutesCount(routesCount.getValue().intValue());
        profile.setFuelPrice(fuelPrice.getValue().intValue());
        profile.setPathPriority(pathPriority.getValue());
        profile.setJumpTime(jumpTime.getValue().intValue());
        profile.setLandingTime(landingTime.getValue().intValue());
        profile.setTakeoffTime(takeoffTime.getValue().intValue());
        profile.setRechargeTime(rechargeTime.getValue().intValue());

        Main.SETTINGS.edce().setActive(edceActive.isSelected());
        Main.SETTINGS.edce().setInterval(edceInterval.getValue().intValue());
    }

    public void showDialog(Parent parent, Parent content){
        if (dlg == null){
            createDialog(parent, content);
        }
        init();
        dlg.showAndWait();
    }

}

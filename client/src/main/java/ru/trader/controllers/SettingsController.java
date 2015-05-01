package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.EMDNUpdater;
import ru.trader.Main;
import ru.trader.core.MarketAnalyzer;
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
    private NumberField segmentSize;
    @FXML
    private NumberField pathsCount;

    private Dialog<ButtonType> dlg;

    @FXML
    private void initialize(){
        init();
    }

    private void init(){
        emdnSubServ.setText(Main.SETTINGS.getEMDNSub());
        emdnOn.setSelected(Main.SETTINGS.getEMDNActive());
        emdnUpdateOnly.setSelected(Main.SETTINGS.getEMDNUpdateOnly());
        emdnUpdateTime.setValue(Main.SETTINGS.getEMDNAutoUpdate());
        segmentSize.setValue(Main.SETTINGS.getSegmentSize());
        pathsCount.setValue(Main.SETTINGS.getPathsCount());
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
        Main.SETTINGS.setEMDNSub(emdnSubServ.getText());
        EMDNUpdater.setSub(emdnSubServ.getText());
        Main.SETTINGS.setEMDNActive(emdnOn.isSelected());
        EMDNUpdater.setActivate(emdnOn.isSelected());
        Main.SETTINGS.setEMDNUpdateOnly(emdnUpdateOnly.isSelected());
        EMDNUpdater.setUpdateOnly(emdnUpdateOnly.isSelected());
        Main.SETTINGS.setEMDNAutoUpdate(emdnUpdateTime.getValue().longValue());
        EMDNUpdater.setInterval(emdnUpdateTime.getValue().longValue());
        MarketAnalyzer analyzer = MainController.getMarket().getAnalyzer();
        Main.SETTINGS.setSegmentSize(segmentSize.getValue().intValue());
        analyzer.setSegmentSize(segmentSize.getValue().intValue());
        Main.SETTINGS.setPathsCount(pathsCount.getValue().intValue());
        analyzer.setPathsCount(pathsCount.getValue().intValue());

    }

    public void showDialog(Parent parent, Parent content){
        if (dlg == null){
            createDialog(parent, content);
        }
        init();
        dlg.showAndWait();
    }

}

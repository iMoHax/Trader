package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.EMDNUpdater;
import ru.trader.Main;
import ru.trader.core.MarketAnalyzer;
import ru.trader.model.MarketModel;
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

    private final Action actSave = new DialogAction(Localization.getString("dialog.button.save"), ButtonBar.ButtonType.OK_DONE, false, true, false, (e) -> save());


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

    public Action showDialog(Parent parent, Parent content){
        init();
        Dialog dlg = new Dialog(parent, Localization.getString("settings.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(actSave, Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        return dlg.show();
    }

}

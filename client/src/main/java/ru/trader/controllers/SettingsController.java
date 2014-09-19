package ru.trader.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.EMDNUpdater;
import ru.trader.Main;
import ru.trader.World;
import ru.trader.core.MarketAnalyzer;
import ru.trader.emdn.EMDN;
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

    private final Action actSave = new AbstractAction(Localization.getString("dialog.button.save")) {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }

        @Override
        public void handle(ActionEvent event) {
            Dialog dlg = (Dialog) event.getSource();
            save();
            dlg.hide();
        }
    };

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
        MarketModel market = MainController.getMarket();
        Main.SETTINGS.setSegmentSize(segmentSize.getValue().intValue());
        market.setSegmetnSize(segmentSize.getValue().intValue());
        Main.SETTINGS.setPathsCount(pathsCount.getValue().intValue());
        market.setLimit(pathsCount.getValue().intValue());

    }

    public Action showDialog(Parent parent, Parent content){
        init();
        Dialog dlg = new Dialog(parent, Localization.getString("settings.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(actSave, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        return dlg.show();
    }

}

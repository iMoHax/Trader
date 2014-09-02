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
import ru.trader.Main;
import ru.trader.World;
import ru.trader.emdn.EMDN;
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
    private void initialize(){
        emdnOn.setSelected(Main.SETTINGS.getEMDNActive());
        emdnSubServ.setText(Main.SETTINGS.getEMDNSub());
        emdnUpdateOnly.setSelected(Main.SETTINGS.getEMDNUpdateOnly());
        emdnUpdateTime.setValue(Main.SETTINGS.getEMDNAutoUpdate());
    }

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

    private void save() {
        Main.SETTINGS.setEMDNActive(emdnOn.isSelected());
        Main.SETTINGS.setEMDNSub(emdnSubServ.getText());
        Main.SETTINGS.setEMDNUpdateOnly(emdnUpdateOnly.isSelected());
        Main.SETTINGS.setEMDNAutoUpdate(emdnUpdateTime.getValue().longValue());
        EMDN emdn = World.getEmdn();
        emdn.connectTo(emdnSubServ.getText());
        if (emdnOn.isSelected()){
            emdn.start();
        } else {
            emdn.shutdown();
        }
    }

    public Action showDialog(Parent parent, Parent content){
        Dialog dlg = new Dialog(parent, Localization.getString("settings.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(actSave, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        return dlg.show();
    }

}

package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.trader.model.PathRouteModel;
import ru.trader.view.support.Localization;

import java.util.Collection;

public class PathsController {
    private final Action OK = new AbstractAction("OK") {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }


        @Override
        public void handle(ActionEvent event) {
            Dialog dlg = (Dialog) event.getSource();
            dlg.hide();
        }
    };

    @FXML
    private TableView<PathRouteModel> tblPaths;


    @FXML
    private void initialize() {

    }


    public PathRouteModel showDialog(Parent parent, Parent content, Collection<PathRouteModel> paths) {

        init(paths);

        Dialog dlg = new Dialog(parent, Localization.getString("paths.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        PathRouteModel res = dlg.show() == OK ? getPath() : null;
        tblPaths.getItems().clear();
        return res;
    }

    public PathRouteModel getPath(){
        return tblPaths.getSelectionModel().getSelectedItem();
    }

    private void init(Collection<PathRouteModel> paths) {
        tblPaths.getSelectionModel().clearSelection();
        tblPaths.setItems(FXCollections.observableArrayList(paths));
        if (tblPaths.getSortOrder().size()>0)
            tblPaths.sort();
    }

}

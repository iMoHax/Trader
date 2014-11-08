package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import ru.trader.model.PathRouteModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;

import java.util.Collection;
import java.util.List;

public class PathsController {
    private final Action OK = new DialogAction("OK", ButtonBar.ButtonType.OK_DONE, false, true, false);

    @FXML
    private TableView<PathRouteModel> tblPaths;
    private final List<PathRouteModel> paths = FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        BindingsHelper.setTableViewItems(tblPaths, paths);
    }


    public PathRouteModel showDialog(Parent parent, Parent content, Collection<PathRouteModel> paths) {

        init(paths);

        Dialog dlg = new Dialog(parent, Localization.getString("paths.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        PathRouteModel res = dlg.show() == OK ? getPath() : null;
        paths.clear();
        return res;
    }

    public PathRouteModel getPath(){
        return tblPaths.getSelectionModel().getSelectedItem();
    }

    private void init(Collection<PathRouteModel> paths) {
        tblPaths.getSelectionModel().clearSelection();
        this.paths.clear();
        this.paths.addAll(paths);
    }

}

package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import ru.trader.model.RouteModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;

import java.util.List;

public class PathsController {
    private final Action OK = new DialogAction("OK", ButtonBar.ButtonType.OK_DONE, false, true, false);

    @FXML
    private TableView<RouteModel> tblPaths;
    private final List<RouteModel> paths = FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        BindingsHelper.setTableViewItems(tblPaths, paths);
    }


    public RouteModel showDialog(Parent parent, Parent content, ObservableList<RouteModel> paths) {

        init(paths);

        Dialog dlg = new Dialog(parent, Localization.getString("paths.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        RouteModel res = dlg.show() == OK ? getPath() : null;
        paths.clear();
        return res;
    }

    public RouteModel getPath(){
        return tblPaths.getSelectionModel().getSelectedItem();
    }

    private void init(ObservableList<RouteModel> paths) {
        tblPaths.getSelectionModel().clearSelection();
        this.paths.clear();
        this.paths.addAll(paths);
        paths.addListener((ListChangeListener<RouteModel>) l -> {
            while (l.next()) {
                if (l.wasAdded()) {
                    this.paths.addAll(l.getAddedSubList());
                }
            }
        });
    }

}

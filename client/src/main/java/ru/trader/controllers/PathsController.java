package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import ru.trader.model.PathRouteModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;

import java.util.List;
import java.util.Optional;

public class PathsController {
    @FXML
    private TableView<PathRouteModel> tblPaths;
    private final List<PathRouteModel> paths = FXCollections.observableArrayList();
    private final ListChangeListener<PathRouteModel> PATHS_CHANGE_LISTENER = l -> {
        while (l.next()) {
            if (l.wasAdded()) {
                this.paths.addAll(l.getAddedSubList());
            }
        }
    };

    private Dialog<PathRouteModel> dlg;
    private ObservableList<PathRouteModel> p;


    @FXML
    private void initialize() {
        BindingsHelper.setTableViewItems(tblPaths, paths);
    }


    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("paths.title"));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return getPath();
            }
            return null;
        });
        dlg.setResizable(false);
    }

    private void fill(ObservableList<PathRouteModel> paths){
        this.paths.clear();
        this.paths.addAll(paths);
        p = paths;
        p.addListener(PATHS_CHANGE_LISTENER);
    }

    private void clear(){
        tblPaths.getSelectionModel().clearSelection();
        this.paths.clear();
        p.removeListener(PATHS_CHANGE_LISTENER);
        p = null;
    }

    public  Optional<PathRouteModel> showDialog(Parent parent, Parent content, ObservableList<PathRouteModel> paths) {
        if (dlg == null){
            createDialog(parent, content);
        }
        fill(paths);
        Optional<PathRouteModel> res = dlg.showAndWait();
        clear();
        return res;
    }

    public PathRouteModel getPath(){
        return tblPaths.getSelectionModel().getSelectedItem();
    }

}

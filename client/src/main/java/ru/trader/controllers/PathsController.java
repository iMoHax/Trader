package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.LongStringConverter;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.trader.graph.PathRoute;
import ru.trader.model.OrderModel;
import ru.trader.model.PathRouteModel;

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


    public void showDialog(Parent parent, Parent content, Collection<PathRouteModel> paths) {

        init(paths);

        Dialog dlg = new Dialog(parent, String.format("Доступные маршруты"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        dlg.show();
    }


    private void init(Collection<PathRouteModel> paths) {
        tblPaths.getSelectionModel().clearSelection();
        tblPaths.setItems(FXCollections.observableArrayList(paths));
        if (tblPaths.getSortOrder().size()>0)
            tblPaths.sort();
    }

}

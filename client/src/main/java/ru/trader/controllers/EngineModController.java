package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import ru.trader.core.Engine;
import ru.trader.core.ModEngine;
import ru.trader.model.MarketModel;
import ru.trader.view.support.EngineStringConverter;
import ru.trader.view.support.Localization;
import ru.trader.view.support.NumberField;

import java.util.Optional;

public class EngineModController {
    @FXML
    private ComboBox<Engine> type;
    @FXML
    private NumberField optMass;

    private Dialog<Engine> dlg;
    private MarketModel market;

    @FXML
    private void initialize() {
        type.setItems(FXCollections.observableArrayList(Engine.getEngines()));
        type.setConverter(new EngineStringConverter());
        type.getSelectionModel().selectFirst();
        optMass.setValue(0.0);
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("dialog.engine.title"));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(Dialogs.OK, Dialogs.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == Dialogs.OK) {
                return new ModEngine(type.getValue(), optMass.getValue().doubleValue());
            }
            return null;
        });
        dlg.setResizable(false);
    }


    public Optional<Engine> showDialog(Parent parent, Parent content) {
        if (dlg == null){
            createDialog(parent, content);
        }
        return dlg.showAndWait();
    }


}

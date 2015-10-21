package ru.trader.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.Main;
import ru.trader.view.support.Localization;

import java.util.Optional;


public class LoginController {
    private final static Logger LOG = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField email;
    @FXML
    private PasswordField password;

    private Dialog<Pair<String, String>> dialog;

    private void createDialog(Parent owner, Parent content){
        dialog = new Dialog<>();
        if (owner != null) dialog.initOwner(owner.getScene().getWindow());
        dialog.setTitle(Localization.getString("login.title"));
        dialog.setHeaderText(Localization.getString("login.header"));
        dialog.getDialogPane().setContent(content);
        ButtonType loginButtonType = new ButtonType(Localization.getString("login.text.login"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        email.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
            Main.SETTINGS.getEdce().setEmail(newValue);
        });
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(email.getText(), password.getText());
            }
            return null;
        });
    }

    private void clear(){
        password.clear();
    }

    public Optional<Pair<String, String>> showDialog(Parent parent, Parent content){
        if (dialog == null){
            createDialog(parent, content);
        }
        Platform.runLater(email::requestFocus);
        Optional<Pair<String, String>> result = dialog.showAndWait();
        clear();
        return result;
    }
}

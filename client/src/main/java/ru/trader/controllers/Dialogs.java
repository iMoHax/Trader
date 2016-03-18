package ru.trader.controllers;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import ru.trader.view.support.Localization;

public class Dialogs {
    public static ButtonType OK;
    public static ButtonType SAVE;
    public static ButtonType CANCEL;
    public static ButtonType YES;
    public static ButtonType NO;
    public static ButtonType LOGIN;

    static {
        initButtons();
    }

    public static void initButtons(){
        OK = new ButtonType(Localization.getString("dialog.button.ok"), ButtonBar.ButtonData.OK_DONE);
        SAVE = new ButtonType(Localization.getString("dialog.button.save"), ButtonBar.ButtonData.OK_DONE);
        CANCEL = new ButtonType(Localization.getString("dialog.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        YES = new ButtonType(Localization.getString("dialog.button.yes"), ButtonBar.ButtonData.YES);
        NO = new ButtonType(Localization.getString("dialog.button.no"), ButtonBar.ButtonData.NO);
        LOGIN = new ButtonType(Localization.getString("login.text.login"), ButtonBar.ButtonData.OK_DONE);
    }
}

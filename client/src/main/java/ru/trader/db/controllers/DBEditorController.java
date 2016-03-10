package ru.trader.db.controllers;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.trader.controllers.MainController;
import ru.trader.model.MarketModel;

public class DBEditorController {
    private Stage stage;

    public void show(Parent content, boolean toggle) {
        if (stage == null){
            stage = new Stage();
            Scene scene = new Scene(content);
            stage.setScene(scene);
            stage.show();
        } else {
            if (toggle && stage.isShowing()){
                stage.hide();
            } else {
                stage.show();
            }
        }
    }

    public void init(){
        MarketModel market = MainController.getMarket();
        //TODO: add init all controllers
    }

    public void close() {
        if (stage != null){
            stage.close();
            stage = null;
        }
    }
}

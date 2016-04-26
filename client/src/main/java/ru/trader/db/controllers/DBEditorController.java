package ru.trader.db.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DBEditorController {
    private Stage stage;
    @FXML
    private ItemsController itemsController;
    @FXML
    private SystemsController systemsController;
    @FXML
    private StationsController stationsController;


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
        itemsController.init();
        systemsController.init();
        stationsController.init();
    }

    public void close() {
        if (stage != null){
            stage.close();
            stage = null;
        }
    }
}

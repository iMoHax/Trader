package ru.trader.view.support.cells;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.trader.model.StationModel;

public class StationListCell implements Callback<ListView<StationModel>, ListCell<StationModel>> {


    @Override
    public ListCell<StationModel> call(ListView<StationModel> param){
        return new ListCell<StationModel>(){
            private StationModel s;

            @Override
            public void updateItem(StationModel station, boolean empty) {
                super.updateItem(station, empty);
                if (!empty){
                    if (s != station){
                        textProperty().unbind();
                        textProperty().bind(station.asString());
                        s = station;
                    }
                } else {
                    textProperty().unbind();
                    s = null;
                    setText(null);
                    setGraphic(null);
                }
            }


        };
    }
}

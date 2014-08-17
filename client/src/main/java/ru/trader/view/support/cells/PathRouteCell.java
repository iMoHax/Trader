package ru.trader.view.support.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import ru.trader.core.Order;
import ru.trader.graph.PathRoute;
import ru.trader.model.PathRouteModel;

public class PathRouteCell<T> implements Callback<TableColumn<PathRouteModel, T>, TableCell<PathRouteModel, T>> {
    @Override
    public TableCell<PathRouteModel, T> call(TableColumn<PathRouteModel, T> param) {
        return new TableCell<PathRouteModel, T>(){
            @Override
            public void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                if (!empty){
                    PathRoute p = ((PathRouteModel) getTableRow().getItem()).getPath().getRoot();
                    HBox hBox = new HBox();

                    HBox v = new HBox();
                    v.setAlignment(Pos.BOTTOM_CENTER);
                    v.getChildren().add(new Text(p.get().getName()));
                    while (p.hasNext()){
                        p = p.getNext();
                        if (p.isRefill()) v.getChildren().add(GlyphFontRegistry.glyph("FontAwesome|REFRESH"));
                        hBox.getChildren().add(v);

                        VBox dist = new VBox(new Text(String.format("(%+.0f LY)", p.getDistance())));
                        dist.setAlignment(Pos.BASELINE_CENTER);
                        dist.getChildren().add(GlyphFontRegistry.glyph("FontAwesome|LONG_ARROW_RIGHT"));
                        hBox.getChildren().addAll(dist);

                        v = new HBox();
                        v.setAlignment(Pos.BOTTOM_CENTER);
                        v.getChildren().add(new Text(p.get().getName()));
                        v.getChildren().add(new Text(String.format(" (%+.0f) ", p.getMaxProfit())));
                    }
                    hBox.getChildren().add(v);
                    setText(null);
                    setGraphic(hBox);
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }


        };
    }

}

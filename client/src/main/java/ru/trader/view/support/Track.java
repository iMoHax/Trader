package ru.trader.view.support;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.Glyph;
import ru.trader.model.RouteEntryModel;
import ru.trader.model.RouteModel;
import ru.trader.model.StationModel;
import ru.trader.view.support.cells.DistanceCell;

import java.util.ArrayList;
import java.util.List;

public class Track {
    private final static String CSS_ROUTE = "route";
    private final static String CSS_ICONS = "route-icons";
    private final static String CSS_TRACK = "route-track";
    private final static String CSS_TRACK_TEXT = "route-track-text";
    private final static String CSS_SYSTEM = "route-system";
    private final static String CSS_ACTIVE_SYSTEM = "route-active";
    private final static String CSS_TEXT = "route-text";
    private final static String CSS_SYSTEM_TEXT = "route-system-text";
    private final static String CSS_STATION_TEXT = "route-station-text";

    private final RouteModel route;
    private final HBox node = new HBox();
    private final IntegerProperty active;
    private final List<Node> entryNodes;

    public Track(RouteModel route) {
        this.route = route;
        entryNodes = new ArrayList<>(route.getJumps());
        active = new SimpleIntegerProperty(-1);
        node.getStyleClass().add(CSS_ROUTE);
        build();
    }

    private void build(){
        StationModel prev = null;
        for (RouteEntryModel entry : route.getEntries()) {
            if (prev != null){
                VBox track = new VBox();
                VBox.setVgrow(track, Priority.ALWAYS);
                track.getStyleClass().add(CSS_TRACK);

                Text t = new Text(DistanceCell.distanceToString(entry.getStation().getDistance(prev)));
                t.getStyleClass().add(CSS_TRACK_TEXT);
                track.getChildren().addAll(t, Glyph.create("FontAwesome|LONG_ARROW_RIGHT"));

                node.getChildren().addAll(track);
            }
            HBox entryNode = new HBox();
            HBox stationNode = new HBox();
            VBox icons = new VBox();
            VBox.setVgrow(icons, Priority.ALWAYS);

            stationNode.getStyleClass().add(CSS_SYSTEM);
            icons.getStyleClass().add(CSS_ICONS);

            stationNode.getChildren().add(buildText(entry.getStation(), entry.isTransit()));

            if (entry.isBuy()){
                icons.getChildren().add(Glyph.create("FontAwesome|UPLOAD"));
            }
            if (entry.getRefill() > 0){
                icons.getChildren().add(Glyph.create("FontAwesome|REFRESH"));
            }
            if (entry.isSell()){
                icons.getChildren().add(Glyph.create("FontAwesome|DOWNLOAD"));
            }
            entryNode.getChildren().addAll(stationNode, icons);
            node.getChildren().addAll(entryNode);
            final int curIndex = entryNodes.size();
            entryNode.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY){
                    setActive(curIndex);
                }
            });
            entryNodes.add(entryNode);
            prev = entry.getStation();
        }
    }

    private VBox buildText(StationModel station, boolean transit){
        Text systemText = new Text(station.getSystem().getName());
        systemText.getStyleClass().add(CSS_SYSTEM_TEXT);

        VBox text = new VBox(2);
        VBox.setVgrow(text, Priority.ALWAYS);
        text.getStyleClass().add(CSS_TEXT);
        text.getChildren().addAll(systemText);

        if (!transit) {
            Text stationText = new Text(station.getName());
            stationText.getStyleClass().add(CSS_STATION_TEXT);
            Text distanceText = new Text(String.format("%.0f Ls", station.getDistance()));
            distanceText.getStyleClass().add(CSS_STATION_TEXT);
            text.getChildren().addAll(stationText, distanceText);
        }

        return text;
    }

    public int getActive() {
        return active.get();
    }

    public IntegerProperty activeProperty() {
        return active;
    }

    public void setActive(int index){
        if (this.active.get() != -1){
            entryNodes.get(this.active.get()).getStyleClass().remove(CSS_ACTIVE_SYSTEM);
        }
        this.active.setValue(index);
        entryNodes.get(index).getStyleClass().add(CSS_ACTIVE_SYSTEM);
    }

    public Node getNode() {
        return node;
    }
}

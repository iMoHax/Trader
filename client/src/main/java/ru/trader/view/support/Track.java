package ru.trader.view.support;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.Glyph;
import ru.trader.model.RouteEntryModel;
import ru.trader.model.RouteModel;

import java.util.ArrayList;
import java.util.List;

public class Track {
    private final static String CSS_ROUTE = "route";
    private final static String CSS_ROUTE_ENTRY = "route-entry";
    private final static String CSS_ROUTE_CURRENT_ENTRY = "route-current-entry";
    private final static String CSS_ROUTE_MARKER = "route-marker";
    private final static String CSS_SYSTEM = "route-system";
    private final static String CSS_ICONS = "route-icons";
    private final static String CSS_INFO = "route-info";
    private final static String CSS_ACTIVE_SYSTEM = "route-active";
    private final static String CSS_SYSTEM_TEXT = "route-system-text";
    private final static String CSS_STATION_TEXT = "route-station-text";

    private final RouteModel route;
    private final VBox node = new VBox();
    private final IntegerProperty active;
    private final List<Node> entryNodes;

    public Track(RouteModel route) {
        this.route = route;
        entryNodes = new ArrayList<>(route.getJumps());
        active = new SimpleIntegerProperty(-1);
        node.getStyleClass().add(CSS_ROUTE);
        build();
        route.currentEntryProperty().addListener((ov , o, n) -> updateCurrentEntry(o.intValue(), n.intValue()));
    }

    private void build(){
        for (RouteEntryModel entry : route.getEntries()) {
            HBox entryNode = new HBox();
            entryNode.getStyleClass().add(CSS_ROUTE_ENTRY);
            VBox marker = new VBox();
            marker.getStyleClass().add(CSS_ROUTE_MARKER);
            marker.getChildren().add(new Circle(5));
            entryNode.getChildren().add(marker);
            VBox stationNode = buildStationNode(entry);
            HBox.setHgrow(stationNode, Priority.ALWAYS);
            VBox icons = buildIconsNode(entry);
            VBox info = buildInfoNode(entry);
            entryNode.getChildren().addAll(stationNode, icons, info);
            node.getChildren().addAll(entryNode);
            final int curIndex = entryNodes.size();
            entryNode.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    setActive(curIndex);
                }
            });
            entryNodes.add(entryNode);
        }
        updateCurrentEntry(-1, route.getCurrentEntry());
    }

    private VBox buildStationNode(RouteEntryModel entry){
        VBox node = new VBox();
        node.getStyleClass().add(CSS_SYSTEM);
        VBox.setVgrow(node, Priority.ALWAYS);
        Text systemText = new Text(entry.getStation().getSystem().getName());
        systemText.getStyleClass().add(CSS_SYSTEM_TEXT);
        node.getChildren().addAll(systemText);
        if (!entry.isTransit()) {
            Text stationText = new Text(entry.getStation().getName());
            stationText.getStyleClass().add(CSS_STATION_TEXT);
            node.getChildren().addAll(stationText);
        }
        return node;
    }

    private VBox buildIconsNode(RouteEntryModel entry){
        VBox icons = new VBox();
        icons.getStyleClass().add(CSS_ICONS);
        VBox.setVgrow(icons, Priority.ALWAYS);
        if (entry.isBuy()){
            icons.getChildren().add(Glyph.create("FontAwesome|UPLOAD"));
        }
        if (entry.getRefill() > 0){
            icons.getChildren().add(Glyph.create("FontAwesome|REFRESH"));
        }
        if (entry.isSell()){
            icons.getChildren().add(Glyph.create("FontAwesome|DOWNLOAD"));
        }
        return icons;
    }

    private VBox buildInfoNode(RouteEntryModel entry){
        VBox node = new VBox();
        node.getStyleClass().add(CSS_INFO);
        VBox.setVgrow(node, Priority.ALWAYS);
        Text timeText = new Text(ViewUtils.timeToString(entry.getFullTime()));
        Text distanceText = new Text(ViewUtils.distanceToString(entry.getDistance()));
        Text stationDistanceText = new Text(entry.getStation().getSystem().getName());
        if (entry.isTransit()) {
            stationDistanceText.setText("");
        } else {
            stationDistanceText.setText(ViewUtils.stationDistanceToString(entry.getStation().getDistance()));
        }
        node.getChildren().addAll(timeText, distanceText, stationDistanceText);
        return node;
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

    private void updateCurrentEntry(int oldIndex, int newIndex){
        if (oldIndex != -1){
            entryNodes.get(oldIndex).getStyleClass().remove(CSS_ROUTE_CURRENT_ENTRY);
        }
        if (newIndex != -1){
            entryNodes.get(newIndex).getStyleClass().add(CSS_ROUTE_CURRENT_ENTRY);
        }
    }
}

package ru.trader.view.support;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.Glyph;
import ru.trader.analysis.Route;
import ru.trader.analysis.RouteEntry;
import ru.trader.core.Vendor;
import ru.trader.model.RouteModel;

public class RouteNode {
    private final static String CSS_PATH = "path";
    private final static String CSS_ICONS = "path-icons";
    private final static String CSS_TRACK = "path-track";
    private final static String CSS_TRACK_TEXT = "path-track-text";
    private final static String CSS_SYSTEM = "path-system";
    private final static String CSS_TEXT = "path-text";
    private final static String CSS_SYSTEM_TEXT = "path-system-text";
    private final static String CSS_STATION_TEXT = "path-station-text";

    private final Route route;
    private final HBox node = new HBox();

    public RouteNode(RouteModel route) {
        this.route = route.getRoute();
        node.getStyleClass().add(CSS_PATH);
        build();
    }

    private void build(){
        Vendor prev = null;
        for (RouteEntry entry : route.getEntries()) {
            if (prev != null){
                VBox track = new VBox();
                VBox.setVgrow(track, Priority.ALWAYS);
                track.getStyleClass().add(CSS_TRACK);

                Text t = new Text(ViewUtils.distanceToString(entry.getVendor().getDistance(prev)));
                t.getStyleClass().add(CSS_TRACK_TEXT);
                track.getChildren().addAll(t, Glyph.create("FontAwesome|LONG_ARROW_RIGHT"));

                node.getChildren().addAll(track);
            }
            HBox v = new HBox();
            VBox icons = new VBox();
            VBox.setVgrow(icons, Priority.ALWAYS);

            v.getStyleClass().add(CSS_SYSTEM);
            icons.getStyleClass().add(CSS_ICONS);

            v.getChildren().add(buildText(entry.getVendor()));

            if (!entry.getOrders().isEmpty()){
                v.getChildren().add(new Text(String.format(" (%+.0f) ", entry.getProfit())));
                icons.getChildren().add(Glyph.create("FontAwesome|UPLOAD"));
            }
            if (entry.isRefill()){
                icons.getChildren().add(Glyph.create("FontAwesome|REFRESH"));
            }
            if (!entry.isRefill() && entry.isLand()){
                icons.getChildren().add(Glyph.create("FontAwesome|DOWNLOAD"));
            }
            node.getChildren().addAll(v, icons);
            prev = entry.getVendor();
        }
    }

    private VBox buildText(Vendor vendor){
        Text systemText = new Text(vendor.getPlace().getName());
        systemText.getStyleClass().add(CSS_SYSTEM_TEXT);

        VBox text = new VBox(2);
        VBox.setVgrow(text, Priority.ALWAYS);
        text.getStyleClass().add(CSS_TEXT);
        text.getChildren().addAll(systemText);

        if (!vendor.getName().isEmpty()) {
            Text stationText = new Text(vendor.getName());
            stationText.getStyleClass().add(CSS_STATION_TEXT);
            Text distanceText = new Text(String.format("%.0f Ls", vendor.getDistance()));
            distanceText.getStyleClass().add(CSS_STATION_TEXT);
            text.getChildren().addAll(stationText, distanceText);
        }

        return text;
    }

    public Node getNode() {
        return node;
    }
}

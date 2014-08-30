package ru.trader.view.support;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import ru.trader.core.Order;
import ru.trader.graph.PathRoute;
import ru.trader.model.PathRouteModel;

public class RouteNode {
    private final static String CSS_PATH = "path";
    private final static String CSS_ICONS = "path-icons";
    private final static String CSS_TRACK = "path-track";
    private final static String CSS_TRACK_TEXT = "path-track-text";
    private final static String CSS_VENDOR = "path-vendor";

    private final PathRoute path;
    private final HBox node = new HBox();

    public RouteNode(PathRouteModel path) {
        this.path = path.getPath();
        node.getStyleClass().add(CSS_PATH);
        build();
    }

    private void build(){
        HBox v = new HBox();
        VBox icons = new VBox();
        VBox track = new VBox();
        VBox.setVgrow(track, Priority.ALWAYS);
        VBox.setVgrow(icons, Priority.ALWAYS);

        v.getStyleClass().add(CSS_VENDOR);
        icons.getStyleClass().add(CSS_ICONS);
        track.getStyleClass().add(CSS_TRACK);

        PathRoute p = path.getRoot();

        v.getChildren().add(new Text(p.get().getName()));
        Order cargo = null;
        while (p.hasNext()){
            p = p.getNext();
            if (cargo == null && p.getBest() != null){
                cargo = p.getBest();
                icons.getChildren().add(GlyphFontRegistry.glyph("FontAwesome|UPLOAD_ALT"));
            }
            if (p.isRefill()) icons.getChildren().add(GlyphFontRegistry.glyph("FontAwesome|REFRESH"));

            node.getChildren().addAll(v, icons);

            Text t = new Text(String.format("(%.2f LY)", p.getDistance()));
            t.getStyleClass().add(CSS_TRACK_TEXT);


            track.getChildren().addAll(t, GlyphFontRegistry.glyph("FontAwesome|LONG_ARROW_RIGHT"));

            node.getChildren().addAll(track);

            v = new HBox();
            icons = new VBox();
            track = new VBox(0);
            VBox.setVgrow(track, Priority.ALWAYS);
            VBox.setVgrow(icons, Priority.ALWAYS);

            v.getStyleClass().add(CSS_VENDOR);
            icons.getStyleClass().add(CSS_ICONS);
            track.getStyleClass().add(CSS_TRACK);

            v.getChildren().add(new Text(p.get().getName()));
            v.getChildren().add(icons);
            if (cargo != null && cargo.isBuyer(p.get())){
                v.getChildren().add(new Text(String.format(" (%+.0f) ", cargo.getProfit())));
                cargo = null;
                icons.getChildren().add(GlyphFontRegistry.glyph("FontAwesome|DOWNLOAD_ALT"));
            }
        }
        node.getChildren().addAll(v, icons);
    }

    public Node getNode() {
        return node;
    }
}
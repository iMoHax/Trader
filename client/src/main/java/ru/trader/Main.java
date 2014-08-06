package ru.trader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.PropertyConfigurator;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.Screeners;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class Main extends Application {
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
            primaryStage.setTitle("Trader");
            primaryStage.setMinHeight(590);
            primaryStage.setScene(new Scene(Screeners.newScreeners(Main.class.getResource("/view/main.fxml"),getUrl("style.css").toExternalForm())));
            primaryStage.setOnCloseRequest((we)->{
                try {
                    if (World.getMarket().isChange()){
                        Action res = Screeners.showConfirm("Изменения не были сохранены, сохранить?");
                        if (res == Dialog.Actions.YES) World.save();
                        else if (res == Dialog.Actions.CANCEL) we.consume();
                    }
                    Screeners.closeAll();
                } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
                    LOG.error("Ошибка при сохранении",e);
                    Screeners.showException(e);
                }
            });
            loadResources();
            primaryStage.show();
    }


    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.print("Exception in thread \"" + t.getName() + "\" ");
            e.printStackTrace(System.err);
            LOG.error("", e);
            Screeners.showException(e);
        });
        launch(args);
    }


    private static void loadResources() throws IOException {
        Screeners.loadItemDescStage(getUrl(("itemDesc.fxml")));
        Screeners.loadVEditorStage(getUrl(("vEditor.fxml")));
        Screeners.loadAddOfferStage(getUrl(("oEditor.fxml")));
        Screeners.loadOrdersStage(getUrl(("orders.fxml")));
        Screeners.loadTopOrdersStage(getUrl(("topOrders.fxml")));
    }

    private static URL getUrl(String filename) throws MalformedURLException {
        File file = new File("conf"+File.separator+filename);
        if (file.exists()) return file.toURI().toURL();
        return Main.class.getResource("/view/"+filename);
    }


}

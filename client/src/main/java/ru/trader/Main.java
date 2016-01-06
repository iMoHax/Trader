package ru.trader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.apache.log4j.PropertyConfigurator;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.MainController;
import ru.trader.controllers.Screeners;
import ru.trader.view.support.Localization;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class Main extends Application {
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);
    public static Settings SETTINGS = new Settings();
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        SETTINGS = new Settings(new File("profile.properties"));
        SETTINGS.load(World.getMarket());
        Locale locale = SETTINGS.getLocale();
        if (locale != null){
            Localization.setLocale(locale);
        }
        Main.primaryStage = primaryStage;
        loadMainScene();
        loadResources();
        primaryStage.show();
        if (Main.SETTINGS.helper().isVisible()){
            Screeners.showHelper();
        }
        ServicesManager.runAll();
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        KeyBinding.unbind();
        ServicesManager.stopAll();
        SETTINGS.save();
    }

    public static void main(String[] args) {
        //Workaround for fix bug with ComboBox in Win10
        System.setProperty("glass.accessible.force", "false");
        PropertyConfigurator.configure("log4j.properties");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.print("Exception in thread \"" + t.getName() + "\" ");
            e.printStackTrace(System.err);
            LOG.error("", e);
            Screeners.showException(e);
        });
        launch(args);
    }


    public static void changeLocale(Locale locale) throws IOException {
        Localization.setLocale(locale);
        primaryStage.hide();
        Screeners.closeAll();
        MainController.getWorld().refresh();
        loadMainScene();
        loadResources();
        primaryStage.show();
        if (Main.SETTINGS.helper().isVisible()){
            Screeners.showHelper();
        }
    }

    private static void loadMainScene() throws IOException {
        primaryStage.setTitle(Localization.getString("main.title"));
        primaryStage.setMinHeight(500);
        primaryStage.setScene(new Scene(Screeners.newScreeners(Main.class.getResource("/view/main.fxml"), getUrl("style.css").toExternalForm())));
        primaryStage.setOnCloseRequest((we) -> {
            try {
                if (World.getMarket().isChange()) {
                    Action res = Screeners.showConfirm(Localization.getString("dialog.confirm.save"));
                    if (res == Dialog.ACTION_YES) World.save();
                    else if (res == Dialog.ACTION_CANCEL) {
                        we.consume();
                        return;
                    }
                }
                Screeners.closeAll();
            } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
                LOG.error("Error on save world", e);
                Screeners.showException(e);
            }
        });

    }

    private static void loadResources() throws IOException {
        Screeners.loadItemDescStage(getUrl(("itemDesc.fxml")));
        Screeners.loadVEditorStage(getUrl(("vEditor.fxml")));
        Screeners.loadTopOrdersStage(getUrl(("topOrders.fxml")));
        Screeners.loadPathsStage(getUrl(("paths.fxml")));
        Screeners.loadSettingsStage(getUrl(("settings.fxml")));
        Screeners.loadSEditorStage(getUrl(("sEditor.fxml")));
        Screeners.loadFilterStage(getUrl(("filter.fxml")));
        Screeners.loadItemAddStage(getUrl("itemAdd.fxml"));
        Screeners.loadGroupAddStage(getUrl("groupAdd.fxml"));
        Screeners.loadLoginStage(getUrl("login.fxml"));
        Screeners.loadHelperStage(getUrl("helper.fxml"));
        Screeners.loadVendorFilterStage(getUrl("vFilter.fxml"));
    }

    private static URL getUrl(String filename) throws MalformedURLException {
        File file = new File("conf"+File.separator+filename);
        if (file.exists()) return file.toURI().toURL();
        return Main.class.getResource("/view/"+filename);
    }

    public static void copyToClipboard(String string){
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(string);
        clipboard.setContent(content);
    }

}

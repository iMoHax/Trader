package ru.trader;

import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.core.MarketFilter;
import ru.trader.core.Profile;
import ru.trader.core.Ship;
import ru.trader.store.json.JsonStore;

import javax.swing.*;
import java.io.*;
import java.util.Locale;
import java.util.Properties;

public class Settings {
    private final static Logger LOG = LoggerFactory.getLogger(Settings.class);

    private final Properties values = new Properties();
    private final File file;
    private Profile profile;
    private final EDCESettings edce;
    private final EDLogSettings edlog;
    private final HelperSettings helper;
    private final JsonStore jsonStore;
    private MarketFilter filter = new MarketFilter();


    public Settings() {
        this(null);
    }

    public Settings(File file) {
        this.file = file;
        profile = new Profile(new Ship());
        edce = new EDCESettings();
        edlog = new EDLogSettings();
        helper = new HelperSettings();
        jsonStore = new JsonStore();
    }

    public void load(Market market) {
        try (InputStream is = new FileInputStream(file)) {
            values.load(is);
            filter = jsonStore.getFilter(market);
        } catch (FileNotFoundException e) {
            LOG.warn("File {} not found", file);
        } catch (IOException e) {
            LOG.error("Error on load settings", e);
        }
        profile = Profile.readFrom(values, market);
        edce.readFrom(values);
        edlog.readFrom(values);
        helper.readFrom(values);
    }

    public void save(){
        try (OutputStream os = new FileOutputStream(file)) {
            profile.writeTo(values);
            edce.writeTo(values);
            edlog.writeTo(values);
            helper.writeTo(values);
            values.store(os, "settings");
            jsonStore.saveFilter(filter);
        } catch (IOException e) {
            LOG.error("Error on save settings", e);
        }
    }

    public void setLocale(Locale locale){
        values.setProperty("locale", locale.toLanguageTag());
    }

    public Locale getLocale(){
        String locale = values.getProperty("locale");
        return locale != null ? Locale.forLanguageTag(locale): null;
    }

    public void setEMDNActive(boolean active){
        values.setProperty("emdn.active", active ? "1":"0");
    }

    public boolean getEMDNActive(){
        return !"0".equals(values.getProperty("emdn.active","0"));
    }

    public void setEMDNSub(String address){
        values.setProperty("emdn.sub", address);
    }

    public String getEMDNSub(){
        return values.getProperty("emdn.sub","tcp://eddn-relay.elite-markets.net:9500");
    }

    public void setBalance(double balance){
        profile.setBalance(balance);
    }

    public double getBalance(){
        return profile.getBalance();
    }

    public void setCargo(int cargo){
        profile.getShip().setCargo(cargo);
    }

    public long getCargo(){
        return profile.getShip().getCargo();
    }

    public void setTank(double tank){
        profile.getShip().setTank(tank);
    }

    public double getTank(){
        return profile.getShip().getTank();
    }

    public void setJumps(int jumps){
        profile.setJumps(jumps);
    }

    public int getJumps(){
        return profile.getJumps();
    }

    public void setRoutesCount(int routesCount){
        profile.setRoutesCount(routesCount);
    }

    public int getRoutesCount(){
        return profile.getRoutesCount();
    }

    public MarketFilter getFilter(Market market){
        return filter;
    }

    public void setFilter(MarketFilter filter){
        this.filter = filter;
    }

    public Profile getProfile() {
        return profile;
    }

    public EDCESettings edce(){
        return edce;
    }

    public EDLogSettings edlog(){
        return edlog;
    }

    public HelperSettings helper(){
        return helper;
    }

    public final class EDCESettings {
        private final BooleanProperty active;
        private final StringProperty email;
        private final IntegerProperty interval;

        public EDCESettings() {
            interval = new SimpleIntegerProperty();
            email = new SimpleStringProperty();
            active = new SimpleBooleanProperty();
        }

        public boolean isActive() {
            return active.get();
        }

        public BooleanProperty activeProperty() {
            return active;
        }

        public void setActive(boolean active) {
            this.active.set(active);
        }

        public String getEmail() {
            return email.get();
        }

        public StringProperty emailProperty() {
            return email;
        }

        public void setEmail(String email) {
            this.email.set(email);
        }

        public int getInterval() {
            return interval.get();
        }

        public IntegerProperty intervalProperty() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval.set(interval);
        }

        public void readFrom(Properties values){
            setActive(!"0".equals(values.getProperty("edce.active", "0")));
            setEmail(values.getProperty("edce.mail", "example@mail.com"));
            setInterval(Integer.valueOf(values.getProperty("edce.interval", "20")));
        }

        public void writeTo(Properties values){
            values.setProperty("edce.active", isActive() ? "1":"0");
            values.setProperty("edce.mail", getEmail());
            values.setProperty("edce.interval", String.valueOf(getInterval()));
        }


    }

    public final class EDLogSettings {
        private final BooleanProperty active;
        private final StringProperty logDir;

        public EDLogSettings() {
            active = new SimpleBooleanProperty();
            logDir = new SimpleStringProperty();
        }

        public boolean isActive() {
            return active.get();
        }

        public BooleanProperty activeProperty() {
            return active;
        }

        public void setActive(boolean active) {
            this.active.set(active);
        }

        public String getLogDir() {
            return logDir.get();
        }

        public StringProperty logDirProperty() {
            return logDir;
        }

        public void setLogDir(String logDir) {
            this.logDir.set(logDir);
        }

        public void readFrom(Properties values){
            setActive(!"0".equals(values.getProperty("edlog.active", "0")));
            setLogDir(values.getProperty("edlog.dir", "%ProgramFiles%/Frontier/Products/elite-dangerous-64/logs"));
        }

        public void writeTo(Properties values){
            values.setProperty("edlog.active", isActive() ? "1":"0");
            values.setProperty("edlog.dir", getLogDir());
        }

    }


    public final class HelperSettings {
        private final IntegerProperty x;
        private final IntegerProperty y;
        private final BooleanProperty visible;
        private final ObjectProperty<KeyStroke> completeKey;

        public HelperSettings() {
            x = new SimpleIntegerProperty();
            y = new SimpleIntegerProperty();
            visible = new SimpleBooleanProperty();
            completeKey = new SimpleObjectProperty<>();
        }

        public int getX() {
            return x.get();
        }

        public IntegerProperty xProperty() {
            return x;
        }

        public void setX(int x) {
            this.x.set(x);
        }

        public int getY() {
            return y.get();
        }

        public IntegerProperty yProperty() {
            return y;
        }

        public void setY(int y) {
            this.y.set(y);
        }

        public boolean isVisible() {
            return visible.get();
        }

        public BooleanProperty visibleProperty() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible.set(visible);
        }

        public KeyStroke getCompleteKey() {
            return completeKey.get();
        }

        public ObjectProperty<KeyStroke> completeKeyProperty() {
            return completeKey;
        }

        public void setCompleteKey(KeyStroke completeKey) {
            this.completeKey.set(completeKey);
        }

        public void readFrom(Properties values){
            setVisible(!"0".equals(values.getProperty("helper.visible", "0")));
            setX(Integer.valueOf(values.getProperty("helper.x", "100")));
            setY(Integer.valueOf(values.getProperty("helper.y", "100")));
            setCompleteKey(KeyStroke.getKeyStroke(values.getProperty("helper.keys.complete", "pressed END")));
        }

        public void writeTo(Properties values){
            values.setProperty("helper.visible", isVisible() ? "1":"0");
            values.setProperty("helper.x", String.valueOf(getX()));
            values.setProperty("helper.y", String.valueOf(getY()));
            values.setProperty("helper.keys.complete", String.valueOf(getCompleteKey()));
        }
    }

}

package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

public class Settings {
    private final static Logger LOG = LoggerFactory.getLogger(Settings.class);

    private final Properties values = new Properties();
    private final File file;
    private Profile profile;


    public Settings() {
        this.file = null;
        profile = new Profile(new Ship());
    }

    public Settings(File file) {
        this.file = file;
        profile = new Profile(new Ship());
    }

    public void load(Market market) {
        try (InputStream is = new FileInputStream(file)) {
            values.load(is);
        } catch (FileNotFoundException e) {
            LOG.warn("File {} not found", file);
        } catch (IOException e) {
            LOG.error("Error on load settings", e);
        }
        profile = Profile.readFrom(values, market);
    }

    public void save(){
        try (OutputStream os = new FileOutputStream(file)) {
            profile.writeTo(values);
            values.store(os,"settings");
        } catch (IOException e) {
            LOG.error("Error on load settings", e);
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
        return values.getProperty("emdn.sub","tcp://firehose.elite-market-data.net:9050");
    }

    public void setEMDNUpdateOnly(boolean updateOnly){
        values.setProperty("emdn.updateOnly", updateOnly ? "1":"0");
    }

    public boolean getEMDNUpdateOnly(){
        return !"0".equals(values.getProperty("emdn.updateOnly","1"));
    }

    public void setEMDNAutoUpdate(long autoUpdate){
        values.setProperty("emdn.auto", String.valueOf(autoUpdate));
    }

    public long getEMDNAutoUpdate(){
        return Long.valueOf(values.getProperty("emdn.auto","0"));
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

    public int getCargo(){
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
        return MarketFilter.buildFilter(values, market);
    }

    public void setFilter(MarketFilter filter){
        filter.writeTo(values);
    }

    public Profile getProfile() {
        return profile;
    }
}

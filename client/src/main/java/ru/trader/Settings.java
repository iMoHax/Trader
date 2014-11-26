package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class Settings {
    private final static Logger LOG = LoggerFactory.getLogger(Settings.class);

    private final Properties values = new Properties();
    private final File file;

    public Settings() {
        this.file = null;
    }

    public Settings(File file) {
        this.file = file;
    }

    public void load() {
        try (InputStream is = new FileInputStream(file)) {
            values.load(is);
        } catch (FileNotFoundException e) {
            LOG.warn("File {} not found", file);
        } catch (IOException e) {
            LOG.error("Error on load settings", e);
        }
    }

    public void save(){
        try (OutputStream os = new FileOutputStream(file)) {
            values.store(os,"settings");
        } catch (IOException e) {
            LOG.error("Error on load settings", e);
        }
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
        values.setProperty("ship.balance", String.valueOf(balance));
    }

    public double getBalance(){
        return Double.valueOf(values.getProperty("ship.balance","1000"));
    }

    public void setCargo(int cargo){
        values.setProperty("ship.cargo", String.valueOf(cargo));
    }

    public int getCargo(){
        return Integer.valueOf(values.getProperty("ship.cargo","4"));
    }

    public void setTank(double tank){
        values.setProperty("ship.tank", String.valueOf(tank));
    }

    public double getTank(){
        return Double.valueOf(values.getProperty("ship.tank","20"));
    }

    public void setDistance(double distance){
        values.setProperty("ship.distance", String.valueOf(distance));
    }

    public double getDistance(){
        return Double.valueOf(values.getProperty("ship.distance","7"));
    }

    public void setJumps(int jumps){
        values.setProperty("ship.jumps", String.valueOf(jumps));
    }

    public int getJumps(){
        return Integer.valueOf(values.getProperty("ship.jumps","3"));
    }

    public void setSegmentSize(int segmentSize){
        values.setProperty("performance.segment", String.valueOf(segmentSize));
    }

    public int getSegmentSize(){
        return Integer.valueOf(values.getProperty("performance.segment","0"));
    }

    public void setPathsCount(int pathsCount){
        values.setProperty("performance.limit", String.valueOf(pathsCount));
    }

    public int getPathsCount(){
        return Integer.valueOf(values.getProperty("performance.limit","100"));
    }
}

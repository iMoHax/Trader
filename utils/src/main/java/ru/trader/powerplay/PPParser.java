package ru.trader.powerplay;

import au.com.bytecode.opencsv.CSVParser;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PPParser {
    private final static Logger LOG = LoggerFactory.getLogger(PPParser.class);

    private final CSVParser parser;
    private final Market market;
    private boolean canceled;
    private final Collection<PowerData> controllingSystems;

    public PPParser(Market market) {
        this.market = market;
        parser = new CSVParser(',', '\"');
        controllingSystems = new ArrayList<>(100);
    }

    public void cancel(){
        this.canceled = true;
    }

    public void parseSystems(File file) throws IOException {
        parseFile(file, 1, FILE_TYPE.SYSTEMS);
    }

    private void parseFile(File file, int skip, FILE_TYPE type) throws IOException {
        canceled = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int row = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (canceled) break;
                row++;
                if (row <= skip) continue;
                String[] values = parser.parseLine(line);
                switch (type) {
                    case SYSTEMS: parseSystemsFile(values);
                        break;
                    case PREPARE:
                        break;
                }
            }
            if (type == FILE_TYPE.SYSTEMS){
                updatePowerState();
            }
        } finally {
            if (type == FILE_TYPE.SYSTEMS){
                controllingSystems.clear();
            }
        }
    }

    public void parseSystemsFile(String[] values)  {
        // "Id","Power Id","Value","State","Upkeep Default","Upkeep Current","Income","Controlstarsystem Id","Qty For","Qty Against","Thr For","Thr Against","Prediction","В "
        String starSystemName = getName(values[0]);
        POWER power = getPower(values[1]);
        POWER_STATE state = getState(values[3]);
        if (starSystemName == null || power == null || state == null) return;
        if (state == POWER_STATE.CONTROL || state == POWER_STATE.EXPANSION){
            Place place = market.get(starSystemName);
            if (place != null){
                controllingSystems.add(new PowerData(place, power, state));
            } else {
                LOG.warn("Not found system {} for import powerplay data", starSystemName);
            }
        }
    }



    private final static Pattern NAME_REGEXP = Pattern.compile("(.+)\\s+\\((\\d+)\\)");
    @Nullable
    private String getName(String value) {
        Matcher matcher = NAME_REGEXP.matcher(value);
        if (matcher.find()){
            return matcher.group(1);
        } else {
            LOG.warn("Unknown format of name: {} ", value);
            return null;
        }
    }

    @Nullable
    private POWER getPower(String value) {
        String name = getName(value);
        if (name == null) return null;
        switch (name){
            case "A. Lavigny-Duval": return POWER.LAVIGNY_DUVAL;
            case "Aisling Duval": return POWER.DUVAL;
            case "Archon Delaine": return POWER.DELAINE;
            case "Denton Patreus": return POWER.PATREUS;
            case "Edmund Mahon": return POWER.MAHON;
            case "Felicia Winters": return POWER.WINTERS;
            case "Li Yong-Rui": return POWER.YONG_RUI;
            case "Pranav Antal": return POWER.ANTAL;
            case "Yuri Grom": return POWER.GROM;
            case "Zachary Hudson": return POWER.HUDSON;
            case "Zemina Torval": return POWER.TORVAL;
            default:
                LOG.warn("Unknown power name: {}", name);
        }
        return null;
    }

    @Nullable
    private POWER_STATE getState(String value) {
        if (value == null) return null;
        switch (value){
            case "blocked": return POWER_STATE.NONE;
            case "control": return POWER_STATE.CONTROL;
            case "contested": return POWER_STATE.CONTESTED;
            case "takingControl": return POWER_STATE.EXPANSION;
            case "turmoil": return POWER_STATE.NONE;
            default:
                LOG.warn("Unknown power state: {}", value);
        }
        return null;
    }

    private final static double CONTROLLING_RADIUS = 15;
    private void updatePowerState(){
        if (controllingSystems.isEmpty()) return;
        for (Place starSystem : market.get()) {
            if (starSystem.getFaction() == FACTION.NONE) continue;
            if (starSystem.getPowerState() != POWER_STATE.HEADQUARTERS){
                starSystem.setPower(POWER.NONE, POWER_STATE.NONE);
            }
            for (PowerData powerData : controllingSystems){
                if (starSystem.equals(powerData.starSystem)){
                    if (starSystem.getPowerState() != POWER_STATE.HEADQUARTERS){
                        starSystem.setPower(powerData.power, powerData.state);
                    }
                } else {
                    if (powerData.state != POWER_STATE.CONTROL) continue;
                    if (starSystem.getDistance(powerData.starSystem) <= CONTROLLING_RADIUS){
                        if (starSystem.getPowerState() == POWER_STATE.EXPLOITED){
                            if (starSystem.getPower() != powerData.power){
                                starSystem.setPower(powerData.power, POWER_STATE.CONTESTED);
                                break;
                            }
                        } else {
                            if (starSystem.getPowerState() == POWER_STATE.NONE){
                                starSystem.setPower(powerData.power, POWER_STATE.EXPLOITED);
                            } else {
                                LOG.warn("Illegal power state: {}, star system: {}", starSystem.getPower(), starSystem.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    private enum FILE_TYPE {
        SYSTEMS, PREPARE
    }

    private class PowerData {
        private final Place starSystem;
        private final POWER power;
        private final POWER_STATE state;

        private PowerData(Place starSystem, POWER power, POWER_STATE state) {
            this.starSystem = starSystem;
            this.power = power;
            this.state = state;
        }
    }

}

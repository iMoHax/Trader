package ru.trader.edlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EDLogReader extends LogReader {
    private final static Logger LOG = LoggerFactory.getLogger(EDLogReader.class);
    private final static String LOG_FILE_PATTERN = ".+NetLog\\.log$";
    private final static Pattern SYSTEM_CHANGE_REGEXP = Pattern.compile("System (.+) pos (-?[\\d\\.]+),(-?[\\d\\.]+),(-?[\\d\\.]+)");
    private final static Pattern UNDOCK_REGEXP = Pattern.compile("undocked");

    public EDLogReader() {
        super(LOG_FILE_PATTERN);
    }

    @Override
    protected void outLine(String line) {
        super.outLine(line);
        Matcher matcher = SYSTEM_CHANGE_REGEXP.matcher(line);
        if (matcher.find()){
            parseSystem(matcher);
            return;
        }
        matcher = UNDOCK_REGEXP.matcher(line);
        if (matcher.find()){
            undock();
        }
    }

    private void parseSystem(Matcher matcher) {
        String name = matcher.group(1);
        double x = Double.valueOf(matcher.group(2));
        double y = Double.valueOf(matcher.group(3));
        double z = Double.valueOf(matcher.group(4));
        changeSystem(name, x, y, z);
    }

    protected void changeSystem(String name, double x, double y, double z) {
        LOG.debug("System change to {}, coordinates: {},{},{}", name, x, y, z);
    }

    protected void undock() {
        LOG.debug("Undocked");
    }


}

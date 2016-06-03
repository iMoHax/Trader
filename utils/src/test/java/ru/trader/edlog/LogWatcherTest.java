package ru.trader.edlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogWatcherTest {
    private final static Logger LOG = LoggerFactory.getLogger(LogWatcherTest.class);

    private static String readLine(String format, Object... args) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(format, args);
        }
        System.out.print(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        return reader.readLine();
    }

    public static void main(String args[]) throws Exception {
        LOG.info("Test log watcher");
        LogHandler handler = new LogReader(".+\\.log$");
        String path = readLine("Watch dir:");
        LogWatcher watcher = new LogWatcher(handler);
        watcher.start(path);
        Thread.sleep(5*60*1000);
        watcher.stop();
    }

}

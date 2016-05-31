package ru.trader.edlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

public class LogReader implements LogHandler {
    private final static Logger LOG = LoggerFactory.getLogger(LogReader.class);

    private final String pattern;
    private BufferedReader reader;
    private File file;

    public LogReader(String pattern) {
        this.pattern = pattern;
    }

    private void changeFile(File file){
        if (this.file != null && this.file.equals(file)) return;
        LOG.trace("Watch new file {}", file);
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            if (reader != null){
                closeReader();
            }
            reader = new BufferedReader(fileReader);
            this.file = file;
        } catch (FileNotFoundException e) {
            LOG.error("Not found log file", e);
        }
    }

    private void readFile(){
        readFile(false);
    }

    private void readFile(boolean skip){
        LOG.trace("Read file {}", file);
        String line;
        try {
            while ((line = reader.readLine()) != null){
                if (skip) continue;
                outLine(line);
            }
        } catch (IOException e) {
            LOG.error("Error on read log file",e);
        }
    }

    protected void outLine(String line){
        LOG.trace("Reading line:{}", line);
    }

    @Override
    public void createFile(Path file) {
        if (file.toString().matches(pattern)){
            changeFile(file.toFile());
            readFile(true);
        } else {
            LOG.trace("{} Is not log file, skip", file);
        }
    }

    @Override
    public void updateFile(Path file) {
        if (file.toString().matches(pattern)){
            File f = file.toFile();
            if (this.file == null){
                changeFile(f);
                readFile(true);
            } else {
                if (this.file.equals(f)){
                    readFile();
                }
            }
        } else {
            LOG.trace("{} Is not log file, skip", file);
        }

    }

    private void closeReader(){
        if (reader == null) return;
        try {
            reader.close();
        } catch (IOException e) {
            LOG.warn("Error on close old reader", e);
        }
    }

    @Override
    public void close() {
        closeReader();
    }
}

package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestUtils {
    private final static Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    public static String read(InputStream is){
        StringBuilder builder = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
            while ((line = reader.readLine()) != null){
                if (builder.length() > 0){
                    builder.append("\n");
                }
                builder.append(line);
            }
        } catch (IOException e) {
            LOG.error("Error on read file", e);
        }
        return builder.toString();
    }
}

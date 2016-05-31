package ru.trader.edlog;

import java.nio.file.Path;

public interface LogHandler {

    void createFile(Path file);
    void updateFile(Path file);
    void close();
}

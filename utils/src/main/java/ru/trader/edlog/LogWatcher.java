package ru.trader.edlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class LogWatcher {
    private final static Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

    private final Path dir;
    private final LogHandler handler;
    private WatchService watcher;
    private boolean notCancel;
    private Thread thread;

    public LogWatcher(String dir, LogHandler handler) {
        this(FileSystems.getDefault().getPath(dir), handler);
    }

    public LogWatcher(Path dir, LogHandler handler) {
        this.dir = dir;
        this.handler = handler;
        thread = new Thread(){
            @Override
            public void run() {
                watch();
            }
        };
    }

    public void start() throws IOException {
        LOG.debug("Start log watch service, dir {}", dir);
        if (watcher != null){
            throw new IllegalStateException("Watch service already started");
        }
        watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        notCancel = true;
        thread.start();
    }

    private void watch(){
        try {
            while (notCancel) {
                WatchKey key = watcher.poll(5, TimeUnit.SECONDS);
                if (key == null) continue;
                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    Path filename = ev.context();
                    Path file = dir.resolve(filename);
                    if (ev.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                        LOG.trace("File {} was created", file);
                        handler.createFile(file);
                    } else
                    if (ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
                        LOG.trace("File {} was modified", file);
                        handler.updateFile(file);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (InterruptedException | ClosedWatchServiceException e) {
            if (!Thread.currentThread().isInterrupted()){
                Thread.currentThread().interrupt();
            }
        } finally {
            close();
        }

    }

    private void close() {
        if (watcher != null) try {
            watcher.close();
        } catch (IOException e) {
            LOG.warn("Error on close log watcher", e);
        }
        handler.close();
        watcher = null;
    }

    public void stop(){
        LOG.debug("Stop log watch service");
        notCancel = false;
    }
}

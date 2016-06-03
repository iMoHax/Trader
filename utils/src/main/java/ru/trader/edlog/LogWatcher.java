package ru.trader.edlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class LogWatcher {
    private final static Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

    private Path dir;
    private final LogHandler handler;
    private WatchService watcher;
    private WatcherThread thread;

    public LogWatcher(LogHandler handler) {
        this.handler = handler;
        thread = null;
    }

    public boolean isRun() {
        return thread != null && thread.isAlive();
    }

    public void start(String dir) throws IOException {
        start(FileSystems.getDefault().getPath(dir));
    }

    public void start(Path dir) throws IOException {
        LOG.debug("Start log watch service, dir {}", dir);
        if (thread != null){
            throw new IllegalStateException("Watch service already started");
        }
        this.dir = dir;
        watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        Path last = getLastModify();
        if (last != null) handler.createFile(last);
        thread = new WatcherThread();
        thread.start();
    }

    private Path getLastModify(){
        File last = null;
        File[] files = dir.toFile().listFiles();
        if (files == null) return null;
        for (File file : files) {
            if (last == null || last.lastModified() < file.lastModified()){
                last = file;
            }
        }
        return last != null ? last.toPath() : null;
    }

    private void close() {
        if (watcher != null){
            try {
                watcher.close();
            } catch (IOException e) {
                LOG.warn("Error on close log watcher", e);
            }
        }
        handler.close();
        watcher = null;
    }

    public void stop(){
        LOG.debug("Stop log watch service");
        if (thread != null){
            thread.cancel();
            thread = null;
        }
        close();
    }

    private class WatcherThread extends Thread {
        private boolean run;

        private WatcherThread() {
            this.setDaemon(true);
        }

        public void cancel(){
            run = false;
        }

        @Override
        public void run() {
            run = true;
            try {
                while (run) {
                    WatchKey key = watcher.poll(5, TimeUnit.SECONDS);
                    if (key == null){
                        handler.notChanges();
                        continue;
                    }
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
                cancel();
            }
            run = false;
        }
    }
}

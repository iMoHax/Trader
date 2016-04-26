package ru.trader.services;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.MarketAnalyzer;
import ru.trader.core.Profile;
import ru.trader.model.MarketModel;
import ru.trader.view.support.Localization;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AnalyzerTask<T> extends Task<T> {
    private final AnalyzerCallBack callback;
    protected final MarketAnalyzer analyzer;

    private final LongProperty found;
    private final AtomicReference<Long> foundUpdate;

    public AnalyzerTask(MarketModel market, Profile profile) {
        foundUpdate = new AtomicReference<>((long) 0);
        found = new SimpleLongProperty(0);
        callback = new AnalyzerCallBack();
        analyzer = market.getAnalyzer().newInstance(profile, callback);
    }

    public long getFound() {
        return found.get();
    }

    public LongProperty foundProperty() {
        return found;
    }

    protected void updateFound(long value){
        if (Platform.isFxApplicationThread()) {
            this.found.set(value);
        } else {
            if (foundUpdate.getAndSet(value) == null) {
                Platform.runLater(() -> {
                    final long f = foundUpdate.getAndSet(null);
                    found.set(f);
                });
            }
        }

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        callback.cancel();
        return mayInterruptIfRunning && super.cancel(true);
    }

    private class AnalyzerCallBack extends AnalysisCallBack {
        private final AtomicLong max;
        private final AtomicLong counter;
        private final AtomicLong found;

        private AnalyzerCallBack() {
            max = new AtomicLong(0);
            counter = new AtomicLong(0);
            found = new AtomicLong(0);
        }


        @Override
        public void startStage(String id) {
            super.startStage(id);
        }

        @Override
        public String getMessage(String key) {
            return Localization.getString("analyzer."+key);
        }

        @Override
        public void print(String message) {
            updateMessage(message);
        }

        @Override
        public void setMax(long value) {
            max.addAndGet(value);
        }

        @Override
        public void inc() {
            updateProgress(counter.incrementAndGet(), max.get());
            updateFound(found.incrementAndGet());
        }

        @Override
        public void endStage(String id) {
            updateProgress(counter.incrementAndGet(), max.get());
        }
    }
}

package ru.trader.services;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import ru.trader.core.MarketAnalyzer;
import ru.trader.core.MarketAnalyzerCallBack;
import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.graph.Connectable;
import ru.trader.graph.GraphCallBack;
import ru.trader.graph.RouteSearcherCallBack;
import ru.trader.graph.Vertex;
import ru.trader.model.MarketModel;
import ru.trader.view.support.Localization;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AnalyzerTask<T> extends Task<T> {
    private final AnalyzerCallBack callback;
    protected final MarketAnalyzer analyzer;

    private final LongProperty found;
    private final AtomicReference<Long> foundUpdate;

    public AnalyzerTask(MarketModel market) {
        foundUpdate = new AtomicReference<>((long) 0);
        found = new SimpleLongProperty(0);
        analyzer = market.getAnalyzer();
        callback = new AnalyzerCallBack();
        analyzer.setCallback(callback);
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

    public void stop(){
        callback.cancel();
    }

    private class AnalyzerCallBack extends MarketAnalyzerCallBack {
        private final AtomicLong max;
        private final AtomicLong counter;
        private final AtomicLong found;

        private AnalyzerCallBack() {
            max = new AtomicLong(0);
            counter = new AtomicLong(0);
            found = new AtomicLong(0);
        }

        @Override
        protected RouteSearcherCallBack getRouteSearcherCallBackInstance() {
            return new RSCallBack();
        }


        @Override
        protected GraphCallBack<Place> getGraphCallBackInstance() {
            return new GCallBack<Place>();
        }

        @Override
        protected void onEnd() {
            updateProgress(counter.incrementAndGet(), max.get());
            updateMessage(Localization.getString("analyser.finish"));
        }

        @Override
        public void setMax(long value) {
            max.addAndGet(value);
        }

        @Override
        public void inc() {
            updateProgress(counter.incrementAndGet(), max.get());
        }

        private class RSCallBack extends RouteSearcherCallBack {
            @Override
            protected GraphCallBack<Vendor> getGraphCallBackInstance() {
                return new GCallBack<Vendor>(){
                    @Override
                    public void onStartBuild(Vendor from) {
                        updateMessage(String.format(Localization.getString("analyzer.graph.station.build"), from.getPlace().getName(), from.getName()));
                    }

                    @Override
                    public void onStartFind(Vertex<Vendor> from, Vertex<Vendor> to) {
                        if (to != null) {
                            updateMessage(String.format(Localization.getString("analyzer.find.route"),
                                    from.getEntry().getPlace().getName(),
                                    from.getEntry().getName(),
                                    to.getEntry().getPlace().getName(),
                                    to.getEntry().getName()
                            ));
                        } else {
                            updateMessage(String.format(Localization.getString("analyzer.find.routes"),
                                    from.getEntry().getPlace().getName(),
                                    from.getEntry().getName()
                            ));
                        }
                    }
                };
            }
        }

        private class GCallBack<T extends Connectable<T>> extends GraphCallBack<T> {

            @Override
            public void onStartBuild(T from) {
                updateMessage(String.format(Localization.getString("analyzer.graph.build"), from));
            }

            @Override
            public void onEndBuild() {
                updateProgress(counter.incrementAndGet(), max.get());
                updateMessage(Localization.getString("analyzer.graph.success"));
            }

            @Override
            public void onFound() {
                updateFound(found.incrementAndGet());
            }

            @Override
            public void onEndFind() {
                updateMessage(String.format(Localization.getString("analyzer.find.success"), found.get()));
            }

            @Override
            public void setMax(long value) {
                max.addAndGet(value);
            }

            @Override
            public void inc() {
                updateProgress(counter.incrementAndGet(), max.get());
            }
        }
    }
}

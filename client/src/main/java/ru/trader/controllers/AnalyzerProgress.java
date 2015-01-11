package ru.trader.controllers;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.trader.core.MarketAnalyzer;
import ru.trader.core.MarketAnalyzerCallBack;
import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.graph.Connectable;
import ru.trader.graph.GraphCallBack;
import ru.trader.graph.RouteSearcherCallBack;
import ru.trader.graph.Vertex;

import java.util.concurrent.atomic.AtomicLong;

public class AnalyzerProgress {
    private VBox tasks;
    private TaskVisual task;

    public void show(Parent main, String text, MarketAnalyzer analyzer){
        tasks = new VBox(5);
        task = new TaskVisual(text);
        Button button = new Button("Cancel");

        Scene scene = new Scene(tasks, 200, 200);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();

        MarketAnalyzerCallBack callBack = new AnalyzerCallBack(task);
        analyzer.setCallback(callBack);
        button.setOnAction(e -> callBack.cancel());
        createProgress(task);
        tasks.getChildren().add(button);

        task.getSubTasks().addListener((ListChangeListener<TaskVisual>)l -> {
            while (l.next()) {
                if (l.wasRemoved()) {
                    l.getRemoved().forEach(this::removeProgress);
                }
                if (l.wasAdded()) {
                    l.getAddedSubList().forEach(this::createProgress);
                }
            }
        });
    }

    private void createProgress(TaskVisual task){
        HBox hBox = new HBox(10);
        hBox.setUserData(task);
        Label txt = new Label("Процесс");
        ProgressBar bar = new ProgressBar();
        txt.textProperty().bind(task.messageProperty());
        bar.progressProperty().bind(task.countProperty().divide(task.maxProperty()));
        hBox.getChildren().addAll(txt, bar);
        tasks.getChildren().addAll(hBox);
    }

    private void removeProgress(TaskVisual task){
        tasks.getChildren().removeIf(n -> task.equals(n.getUserData()));
    }

    private class TaskVisual {
        private final StringProperty message;
        private final LongProperty count;
        private final LongProperty max;
        private final ObservableList<TaskVisual> subTasks;

        private TaskVisual(String text) {
            message = new SimpleStringProperty(text);
            count = new SimpleLongProperty(0);
            max = new SimpleLongProperty(1);
            subTasks = FXCollections.observableArrayList();
        }

        public String getMessage() {
            return message.get();
        }

        public StringProperty messageProperty() {
            return message;
        }

        public void setMessage(String message) {
            Platform.runLater(() -> this.message.set(message));
        }

        public long getCount() {
            return count.get();
        }

        public LongProperty countProperty() {
            return count;
        }

        public void setCount(long count) {
            Platform.runLater(() -> this.count.set(count));
        }

        public long getMax() {
            return max.get();
        }

        public LongProperty maxProperty() {
            return max;
        }

        public void setMax(long max) {
            Platform.runLater(() -> this.max.set(max));
        }

        public ObservableList<TaskVisual> getSubTasks() {
            return subTasks;
        }

        public void addSubTask(TaskVisual task){
            Platform.runLater(() -> {
                synchronized (subTasks) {
                    subTasks.add(task);
                }
            });
        }

        public void removeSubTask(TaskVisual task){
            Platform.runLater(() -> {
                synchronized (subTasks) {
                    subTasks.remove(task);
                }
            });
        }
    }

    private class AnalyzerCallBack extends MarketAnalyzerCallBack {
        private final TaskVisual task;
        private final AtomicLong count = new AtomicLong();

        private AnalyzerCallBack(TaskVisual task) {
            this.task = task;
            count.set(0);
        }

        @Override
        protected RouteSearcherCallBack getRouteSearcherCallBackInstance() {
            return new RSCallBack(task);
        }


        @Override
        protected GraphCallBack<Place> getGraphCallBackInstance() {
            TaskVisual subtask = new TaskVisual("Build graph of system");
            task.addSubTask(subtask);
            return new GCallBack<Place>(subtask, task);
        }

        @Override
        protected void onEnd() {
            task.setMessage("Finish");
        }

        @Override
        public void setCount(long count) {
            task.setMax(count);
        }

        @Override
        public void inc() {
            task.setCount(count.incrementAndGet());
        }
    }

    private class RSCallBack extends RouteSearcherCallBack {
        private final TaskVisual task;

        private RSCallBack(TaskVisual task) {
            this.task = task;
        }

        @Override
        protected GraphCallBack<Vendor> getGraphCallBackInstance() {
            TaskVisual subtask = new TaskVisual("Build graph of stations");
            task.addSubTask(subtask);
            return new GCallBack<>(subtask, task);
        }
    }

    private class GCallBack<T extends Connectable<T>> extends GraphCallBack<T> {
        private final TaskVisual task;
        private final TaskVisual owner;
        private final AtomicLong count = new AtomicLong();

        private GCallBack(TaskVisual task, TaskVisual owner) {
            this.task = task;
            this.owner = owner;
            count.set(0);
        }

        @Override
        public void onStartBuild(T from) {
            task.setMessage(String.format("Build graph from %s", from));
        }

        @Override
        public void onEndBuild() {
            task.setMessage("");
        }

        @Override
        public void onStartFind(Vertex<T> from, Vertex<T> to) {
            if (to != null) {
                task.setMessage(String.format("Find path from %s graph to %s", from.getEntry(), to.getEntry()));
            } else {
                task.setMessage(String.format("Find path from %s graph", from.getEntry()));
            }
        }

        @Override
        public void onFound() {
            task.setMessage("");
        }

        @Override
        public void onEndFind() {
            owner.removeSubTask(task);
        }

        @Override
        public void setCount(long count) {
            task.setMax(count);
        }

        @Override
        public void inc() {
            task.setCount(count.incrementAndGet());
        }
    }
}

package ru.trader.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ProgressController {
    private Label text;
    private ProgressBar bar;
    private Dialog<ButtonType> dlg;
    private Task task;


    public ProgressController(Parent owner, String title) {
        dlg = new Dialog<>();
        dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(title);
        dlg.setResizable(false);

        createStage();
    }

    private void createStage(){
        text = new Label();
        bar = new ProgressBar();
        bar.setMaxWidth(Double.MAX_VALUE);
        VBox vbox = new VBox(10, text, bar);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setPrefSize(300, 100);

        dlg.getDialogPane().setContent(vbox);
        dlg.getDialogPane().getButtonTypes().addAll(Dialogs.CANCEL);

        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == Dialogs.CANCEL) {
                if (task != null) {
                    task.cancel(false);
                }
            }
            return dialogButton;
        });
    }

    private <T> void bind(Task<T> task, Consumer<T> onSuccess){
        bar.progressProperty().bind(task.progressProperty());
        text.textProperty().bind(task.messageProperty());
        this.task = task;
        task.setOnSucceeded(e -> {
            Platform.runLater(dlg::hide);
            onSuccess.accept(task.getValue());
            unbind();
        });
        task.setOnCancelled(e -> {
            Platform.runLater(dlg::hide);
            onSuccess.accept(task.getValue());
            unbind();
        });

        task.setOnFailed(e -> {
            Platform.runLater(dlg::hide);
            Screeners.showException(task.getException());
        });
    }

    private void unbind(){
        bar.progressProperty().unbind();
        text.textProperty().unbind();
        task = null;
    }

    public <T> void run(Task<T> task, Consumer<T> onSuccess){
        bind(task, onSuccess);
        Platform.runLater(dlg::show);
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public <T> void run(Task<T> task){
        bind(task, t -> {
        });
        Platform.runLater(dlg::show);
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
}

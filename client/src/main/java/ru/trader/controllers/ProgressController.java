package ru.trader.controllers;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import ru.trader.services.*;

import java.util.function.Consumer;

public class ProgressController {
    private Label text;
    private ProgressBar bar;
    private Action cancel;
    private Dialog dlg;
    private final static String TASK_KEY = "task";


    public ProgressController(Parent owner, String title) {
        dlg = new Dialog(owner, title);
        createStage();
    }

    private void createStage(){
        text = new Label();
        bar = new ProgressBar();
        bar.setMaxWidth(Double.MAX_VALUE);
        VBox vbox = new VBox(10, text, bar);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setPrefSize(300, 100);

        dlg.setClosable(false);
        dlg.setContent(vbox);
        cancel = new DialogAction(impl.org.controlsfx.i18n.Localization.asKey("dlg.cancel.button"), ButtonBar.ButtonType.CANCEL_CLOSE, e -> {
            AnalyzerTask<?> task = (AnalyzerTask<?>) cancel.getProperties().get(TASK_KEY);
            if (task != null){
                task.stop();
            }
        });
        dlg.getActions().addAll(cancel);
    }

    private <T> void bind(AnalyzerTask<T> task, Consumer<T> onSuccess){
        bar.progressProperty().bind(task.progressProperty());
        text.textProperty().bind(task.messageProperty());
        cancel.getProperties().put(TASK_KEY, task);
        task.setOnSucceeded(e -> {
            dlg.hide();
            onSuccess.accept(task.getValue());
            unbind();
        });
        task.setOnCancelled(e -> {
            dlg.hide();
            onSuccess.accept(task.getValue());
            unbind();
        });

        task.setOnFailed(e -> {
            dlg.hide();
            Screeners.showException(task.getException());
        });
    }

    private void unbind(){
        bar.progressProperty().unbind();
        text.textProperty().unbind();
        cancel.getProperties().remove(TASK_KEY);
    }

    public <T> void run(AnalyzerTask<T> task, Consumer<T> onSuccess){
        bind(task, onSuccess);
        Platform.runLater(dlg::show);
        new Thread(task).start();
    }


}

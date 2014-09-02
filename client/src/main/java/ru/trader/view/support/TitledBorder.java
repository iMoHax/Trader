package ru.trader.view.support;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class TitledBorder extends StackPane
{
    private Label titleLabel = new Label();
    private StackPane contentPane = new StackPane();
    private Node content;
    private final static String CSS_CONTENT = "bordered-titled-content";
    private final static String CSS_TITLE = "bordered-titled-title";
    private final static String CSS_BORDER = "bordered-titled-border";


    public void setContent(Node content){
        content.getStyleClass().add(CSS_CONTENT);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(content);
    }

    public Node getContent(){
        return content;
    }

    public void setTitle(String title){
        titleLabel.setText(" " + title + " ");
    }

    public String getTitle(){
        return titleLabel.getText();
    }

    public TitledBorder(){
        StackPane.setAlignment(titleLabel, Pos.TOP_CENTER);
        titleLabel.getStyleClass().add(CSS_TITLE);
        getStyleClass().add(CSS_BORDER);
        getChildren().addAll(titleLabel, contentPane);
    }

}
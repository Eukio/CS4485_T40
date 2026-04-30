package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class HomeScene extends BorderPane {

    public HomeScene(HelloApplication mainApp) {
        setHomeScene(mainApp);
    }

    public void setHomeScene(HelloApplication mainApp) {
        NavBar navBar = new NavBar(mainApp, "home");
        setTop(navBar);

        Text welcomeLine = new Text("Welcome to_\n");
        welcomeLine.getStyleClass().add("hero-text");

        Text titleLine = new Text("Sentence Builder");
        titleLine.getStyleClass().add("hero-text-accent");

        TextFlow heroText = new TextFlow(welcomeLine, titleLine);

        Button startBtn = new Button("Start Here");
        startBtn.getStyleClass().add("button");
        startBtn.setOnAction(e -> {
            try { mainApp.showUploadFilesScene(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        Text credit = new Text("CS4485_Team40");
        credit.setStyle("-fx-fill: #aaaaaa; -fx-font-size: 13px;"); // 11 * 1.2

        VBox centerBox = new VBox(29, heroText, startBtn); // 24 * 1.2
        centerBox.setPadding(new Insets(0, 0, 0, 72)); // 60 * 1.2
        centerBox.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBox = new VBox(credit);
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.setPadding(new Insets(0, 19, 12, 0)); // 16/10 * 1.2

        setCenter(centerBox);
        setBottom(bottomBox);
        setStyle("-fx-background-color: white;");
    }
}
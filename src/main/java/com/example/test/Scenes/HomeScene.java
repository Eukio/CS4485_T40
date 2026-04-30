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
        // ── NavBar ──────────────────────────────────────────────
        NavBar navBar = new NavBar(mainApp);
        setTop(navBar);

        // ── Hero text: "Welcome to_" + "Sentence Builder" ───────
        Text welcomeLine = new Text("Welcome to_\n");
        welcomeLine.setFont(Font.font("SansSerif", FontWeight.NORMAL, 48));
        welcomeLine.setStyle("-fx-fill: #1a1a1a;");

        Text titleLine = new Text("Sentence Builder");
        titleLine.setFont(Font.font("SansSerif", FontWeight.NORMAL, 48));
        titleLine.setStyle("-fx-fill: " + HelloApplication.DARKNAVY + ";");

        TextFlow heroText = new TextFlow(welcomeLine, titleLine);

        Button startBtn = new Button("Start Here");
        startBtn.setOnAction(e -> {
            try { mainApp.showUploadFilesScene(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        // ── Footer credit ────────────────────────────────────────
        Text credit = new Text("CS4485_Team40");
        credit.setStyle("-fx-fill: #aaaaaa; -fx-font-size: 11px;");

        // ── Layout ───────────────────────────────────────────────
        VBox centerBox = new VBox(16, heroText, startBtn);
        centerBox.setPadding(new Insets(40, 0, 0, 48));

        VBox bottomBox = new VBox(credit);
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.setPadding(new Insets(0, 16, 10, 0));

        setCenter(centerBox);
        setBottom(bottomBox);
        setStyle("-fx-background-color: white;");
    }
}
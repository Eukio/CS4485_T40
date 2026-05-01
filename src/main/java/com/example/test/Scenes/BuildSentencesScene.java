package com.example.test.Scenes;

import com.example.test.HelloApplication;
import com.example.test.backend.SentenceBuilder;
import com.example.test.backend.SentenceHistory;
import com.example.test.backend.WordService;
import com.example.test.db.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class BuildSentencesScene extends BorderPane {

    private final String[] algoNames = {"Greedy", "Random Weighted", "Temperature", "BPE Markov"};
    private final int[] algorithmOptions = {0};

    public BuildSentencesScene(HelloApplication mainApp) {
        NavBar navBar = new NavBar(mainApp, "build");
        setTop(navBar);
        setBuildSentencesScene(mainApp);
    }

    public void setBuildSentencesScene(HelloApplication mainApp) {

        // ================= HERO TEXT =================
        Text title0 = new Text("Build ");
        title0.getStyleClass().add("hero-text");

        Text title1 = new Text("Sentences_");
        title1.getStyleClass().add("hero-text-accent");

        TextFlow heroText = new TextFlow(title0, title1);

        Label subtitleLabel = new Label("Build a sentence using any of our algorithms");
        subtitleLabel.getStyleClass().add("upload-label");

        // ================= BUTTONS =================
        Button generateButton = new Button("Generate Sentence");
        generateButton.getStyleClass().add("generate-button");
        generateButton.setPrefSize(180, 50);

        Button algoButton = new Button(algoNames[algorithmOptions[0]]);
        algoButton.getStyleClass().add("algo-button");
        algoButton.setPrefSize(180, 50);

        algoButton.setOnAction(e -> {
            algorithmOptions[0] = (algorithmOptions[0] + 1) % algoNames.length;
            algoButton.setText(algoNames[algorithmOptions[0]]);
        });

        // ================= OUTPUT FIELD (read-only) =================
        TextArea outputField = new TextArea("Your generated sentence will appear here...");
        outputField.setEditable(false);
        outputField.setWrapText(true);
        outputField.setMaxWidth(Double.MAX_VALUE);
        outputField.setPrefRowCount(2);
        outputField.setStyle(
                "-fx-background-color: #D3DFFF;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: transparent;"
        );

        // ================= SENTENCE HISTORY =================
        VBox historyItemsBox = new VBox(12);
        historyItemsBox.setPadding(new Insets(12));
        historyItemsBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;"
        );

        ScrollPane historyScroll = new ScrollPane(historyItemsBox);
        historyScroll.setFitToWidth(true);
        historyScroll.setPrefHeight(200);
        historyScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Label historyTitle = new Label("Sentence History");
        historyTitle.setStyle(
                "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        // Collapse/expand toggle
        final boolean[] expanded = {true};
        Button toggleButton = new Button("∨");
        toggleButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4e60ba;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;"
        );
        toggleButton.setOnAction(e -> {
            expanded[0] = !expanded[0];
            historyScroll.setVisible(expanded[0]);
            historyScroll.setManaged(expanded[0]);
            toggleButton.setText(expanded[0] ? "∨" : "∧");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox historyHeader = new HBox(historyTitle, spacer, toggleButton);
        historyHeader.setAlignment(Pos.CENTER_LEFT);

        VBox historyPanel = new VBox(12, historyHeader, historyScroll);
        historyPanel.setPadding(new Insets(20));
        historyPanel.setStyle(
                "-fx-background-color: #D3DFFF;" +
                        "-fx-background-radius: 12;"
        );

        // ================= GENERATE ACTION =================
        generateButton.setOnAction(e -> {
            try {
                WordService wordService = mainApp.getWordService();
                String sentence = new SentenceBuilder(wordService)
                        .buildSentence(null, algorithmOptions[0]);

                outputField.setText(sentence);

                // Add to history
                Label historyEntry = new Label(sentence);
                historyEntry.setWrapText(true);
                historyEntry.setMaxWidth(Double.MAX_VALUE);
                historyEntry.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 14px;");
                historyItemsBox.getChildren().add(0, historyEntry);

                // Save to DB
                DatabaseManager dbManager = mainApp.getDbManager();
                if (dbManager != null) {
                    new SentenceHistory(dbManager).save(sentence, algoNames[algorithmOptions[0]]);
                }

            } catch (Exception ex) {
                outputField.setText("Error generating sentence: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ================= LAYOUT =================
        HBox buttonRow = new HBox(12, generateButton, algoButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        VBox centerBox = new VBox(16, heroText, subtitleLabel, buttonRow, outputField, historyPanel);
        centerBox.setPadding(new Insets(0, 72, 24, 72));
        centerBox.setAlignment(Pos.CENTER_LEFT);

        setCenter(centerBox);
        setStyle("-fx-background-color: white;");
    }
}
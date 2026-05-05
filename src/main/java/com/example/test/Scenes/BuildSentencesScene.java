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

//Sandra - UI connect frontend/backend
public class BuildSentencesScene extends BorderPane {

    private final String[] algoNames = {"Greedy", "Random Weighted", "Temperature", "BPE Markov"};
    private final int[] algorithmOptions = {0};

    public BuildSentencesScene(HelloApplication mainApp) {
        //Sets NavBar
        NavBar navBar = new NavBar(mainApp, "build");
        setTop(navBar);

        setBuildSentencesScene(mainApp);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
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

        Button algoButton = new Button(algoNames[algorithmOptions[0]]);
        algoButton.getStyleClass().add("algo-button");

        algoButton.setOnAction(e -> {
            algorithmOptions[0] = (algorithmOptions[0] + 1) % algoNames.length;
            algoButton.setText(algoNames[algorithmOptions[0]]);
        });

        // ================= OUTPUT FIELD (editable) =================
        TextArea outputField = new TextArea();
        outputField.setPromptText("Type here or generate a sentence...");
        outputField.setEditable(true);
        outputField.setWrapText(true);
        outputField.setMaxWidth(Double.MAX_VALUE);
        outputField.setPrefRowCount(3);
        outputField.setStyle(
                "-fx-background-color: #D3DFFF;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 5;" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-size: 20px;"
        );

        // ================= SENTENCE HISTORY =================
        VBox historyItemsBox = new VBox(12);
        historyItemsBox.setPadding(new Insets(6));

        ScrollPane historyScroll = new ScrollPane(historyItemsBox);
        historyScroll.setFitToWidth(true);
        historyScroll.setPrefHeight(200);
        historyScroll.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background: white;"
        );
        historyScroll.setVisible(false);
        historyScroll.setManaged(false);

        Label historyTitle = new Label("Sentence History");
        historyTitle.setStyle(
                "-fx-font-family: 'Verdana';" +
                        "-fx-font-size: 20px;"
        );

        final boolean[] expanded = {false};
        Button toggleButton = new Button("Show");
        toggleButton.getStyleClass().add("search-button");
        toggleButton.setOnAction(e -> {
            expanded[0] = !expanded[0];
            historyScroll.setVisible(expanded[0]);
            historyScroll.setManaged(expanded[0]);
            toggleButton.setText(expanded[0] ? "Hide" : "Show");
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

                String existing = outputField.getText().trim();
                String lastWord = null;
                if (!existing.isEmpty()) {
                    String cleaned = existing
                            .replaceAll("\\s*\\[END\\]\\.?$", "")
                            .replaceAll("[.!?]+$", "")
                            .trim();
                    lastWord = cleaned.contains(" ")
                            ? cleaned.substring(cleaned.lastIndexOf(" ") + 1).trim()
                            : cleaned;
                }

                String sentence = new SentenceBuilder(wordService)
                        .buildSentence(lastWord, algorithmOptions[0]);

                // Skip empty or punctuation-only sentences
                if (sentence == null || sentence.trim().matches("[\\s.!?]+")) {
                    return;
                }

                // Remove the seed word from the start of the generated sentence if repeated
                String trimmedSentence = sentence;
                if (lastWord != null && sentence.toLowerCase().startsWith(lastWord.toLowerCase())) {
                    trimmedSentence = capitalize(sentence.substring(lastWord.length()).trim());
                }
                // Skip if trimmed result is empty or just punctuation
                if (trimmedSentence == null || trimmedSentence.trim().matches("[\\s.!?]+")) {
                    return;
                }

                if (existing.isEmpty()) {
                    outputField.setText(capitalize(sentence));
                } else {
                    outputField.setText(capitalize(existing + " " + trimmedSentence));
                }

                // Add to history
                Label historyEntry = new Label(capitalize(trimmedSentence));
                historyEntry.setWrapText(true);
                historyEntry.setMaxWidth(Double.MAX_VALUE);
                historyEntry.setStyle(
                        "-fx-font-family: 'Verdana';" +
                                "-fx-font-size: 14px;" +
                                "-fx-text-fill: #1a1a1a;" +
                                "-fx-background-color: transparent;"
                );
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
        centerBox.setPadding(new Insets(20, 72, 24, 72));
        centerBox.setAlignment(Pos.CENTER_LEFT);

        setCenter(centerBox);
        setStyle("-fx-background-color: white;");
    }
}
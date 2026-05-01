package com.example.test.Scenes;

import com.example.test.HelloApplication;
import com.example.test.backend.SentenceHistory;
import com.example.test.backend.WordService;
import com.example.test.db.DatabaseManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class AutoCompleteScene extends BorderPane {
    WordService wordService;
    DatabaseManager dbManager;

    private final int suggestions = 10;
    private final String[] algoNames = {"Greedy", "Random Weighted", "Temperature", "BPE Markov"};

    public AutoCompleteScene(HelloApplication mainApp, WordService wordService, DatabaseManager dbManager) throws IOException {
        this.wordService = wordService;
        this.dbManager = dbManager;
        NavBar navBar = new NavBar(mainApp, "autocomplete");
        setTop(navBar);
        setAutoCompleteScene(mainApp);
    }

    public void setAutoCompleteScene(HelloApplication mainApp) throws IOException {
        Button[] suggestionFields = new Button[suggestions];

        Button algoButton = new Button("Algorithm: Greedy");
        algoButton.getStyleClass().add("algo-button");
        algoButton.setPrefSize(240, 60);
        algoButton.setBackground(new Background(new BackgroundFill(Color.web("#D3DFFF"), new CornerRadii(10), new Insets(10))));

        TextArea typing = new TextArea();
        typing.getStyleClass().add("typing-area");
        typing.setPadding(new Insets(12));

        ScrollPane wordBankScroll = createRightWordBank(suggestionFields);
        wordBankScroll.setFitToWidth(true);
        wordBankScroll.setPrefHeight(320);

        VBox left = createLeftVBox(algoButton, typing, mainApp);
//        left.setPadding(new Insets(10));

        int maxAlgo = 4;
        int[] algorithmOptions = {0};
        algoButton.setOnAction(event -> {
            algorithmOptions[0] = (algorithmOptions[0] + 1) % maxAlgo;
            algoButton.setText("Algorithm: " + algoNames[algorithmOptions[0]]);
            String text = typing.getText().trim();
            if (!text.isEmpty()) {
                String lastWord = text.contains(" ") ?
                        text.substring(text.lastIndexOf(" ") + 1) : text;
                updateWordBank(lastWord, suggestionFields, typing);
            }
        });

        typing.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                String text = typing.getText().trim();
                if (text.isEmpty()) return;
                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
                updateWordBank(lastWord, suggestionFields, typing);
            }
        });

        setCenter(left);
        setRight(wordBankScroll);
//        HBox app = new HBox(left, wordBankScroll);
//        app.setAlignment(Pos.CENTER);
//        HBox.setHgrow(left, Priority.ALWAYS);
//
//        setCenter(app);
    }

    public ScrollPane createRightWordBank(Button[] suggestionFields) throws IOException {
        Text wordBankTitle = new Text("Next Word");
        wordBankTitle.getStyleClass().add("word-bank-title");

        for (int i = 0; i < suggestions; i++) {
            suggestionFields[i] = new Button();
            suggestionFields[i].setVisible(false);
            suggestionFields[i].getStyleClass().add("suggestion-button");
        }

        VBox wordBank = new VBox(wordBankTitle);
        wordBank.getChildren().addAll(suggestionFields);
        wordBank.getStyleClass().add("word-bank");
        wordBank.setSpacing(12);
        wordBank.setPadding(new Insets(12));

        return new ScrollPane(wordBank);
    }

    public VBox createLeftVBox(Button algoButton, TextArea typing, HelloApplication mainApp) throws IOException {
        Text welcome0 = new Text("Autocomplete");
        welcome0.getStyleClass().add("hero-text");

        Text welcome1 = new Text("_");
        welcome1.getStyleClass().add("hero-text-accent");

        TextFlow heroText = new TextFlow(welcome0, welcome1);

        Label typingLabel = new Label("Start typing to see your autocomplete suggestions");
        typingLabel.getStyleClass().add("upload-label");

        HBox buttons = new HBox(12, algoButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button toSentenceBuilderButton = new Button("Continue");
        toSentenceBuilderButton.getStyleClass().add("continue-button");
        toSentenceBuilderButton.setStyle("-fx-background-color: " + HelloApplication.DARKNAVY + ";");
        toSentenceBuilderButton.setOnAction(e -> mainApp.showBuildSentencesScene());

        VBox left = new VBox(24, heroText, typingLabel, buttons, typing, toSentenceBuilderButton);
        left.setPadding(new Insets(0, 72, 40, 72));
        left.setAlignment(Pos.CENTER_LEFT);
        return left;
    }
    private void updateWordBank(String lastWord, Button[] suggestionFields, TextArea typing) {
        try {
            if (wordService.wordExists(lastWord)) {
                long id = wordService.getWordId(lastWord);
                var candidates = wordService.getAutocompleteCandidates(id, suggestions);
                for (int i = 0; i < suggestionFields.length; i++) {
                    if (i < candidates.size()) {
                        String word = candidates.get(i).word();
                        String displayWord = word.equals("[END]") ? "." : word;
                        suggestionFields[i].setText(displayWord);
                        suggestionFields[i].setVisible(true);
                        suggestionFields[i].setOnAction(e -> {
                            String current = typing.getText();
                            if (current.contains(" ")) {
                                typing.setText(current.substring(0, current.lastIndexOf(" ") + 1) + displayWord);
                            } else {
                                typing.setText(displayWord);
                            }
                            Platform.runLater(() -> {
                                typing.positionCaret(typing.getText().length());
                                typing.requestFocus();
                                typing.positionCaret(typing.getText().length());
                            });
                        });
                    } else {
                        suggestionFields[i].setText("");
                        suggestionFields[i].setVisible(false);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching autocomplete candidates: " + e.getMessage());
        }
    }
}
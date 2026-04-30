package com.example.test.Scenes;

import com.example.test.HelloApplication;
import com.example.test.backend.SentenceBuilder;
import com.example.test.backend.SentenceHistory;
import com.example.test.backend.WordService;
import com.example.test.db.DatabaseManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

import static java.lang.System.out;

public class AutoCompleteScene extends BorderPane {
    WordService wordService;
    DatabaseManager dbManager;

    private final int suggestions = 10; //placeholder for number of suggestions to show
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
        algoButton.setPrefSize(240, 72);

        Button generateButton = new Button("Generate Sentence");
        generateButton.getStyleClass().add("generate-button");
        generateButton.setPrefSize(240, 72);

        TextArea typing = new TextArea();
        typing.getStyleClass().add("typing-area");
        typing.setPadding(new Insets(12));

        ScrollPane wordBankScroll = createRightWordBank(suggestionFields);
        wordBankScroll.setFitToWidth(true);
        wordBankScroll.setPrefHeight(320);

        VBox left = createLeftVBox(algoButton, generateButton, typing);
        left.setPadding(new Insets(10));

//        TextArea reportTextArea = new TextArea();

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
//                updateWordBank(lastWord, suggestionFields, typing, reportTextArea);
            }
        });

        generateButton.setOnAction(event -> {
            String text = typing.getText().trim();
            try {
                String cleaned = text.replaceAll("\\s*\\[END\\]\\.?$", "").replaceAll("[.!?]+$", "").trim();
                String lastWord = cleaned.isEmpty() ? null
                        : cleaned.contains(" ") ? cleaned.substring(cleaned.lastIndexOf(" ") + 1).trim()
                        : cleaned;
                out.println("Using algorithm: " + algorithmOptions[0]);
                String sentence = new SentenceBuilder(wordService).buildSentence(lastWord, algorithmOptions[0]);
                out.println("Generated sentence: " + sentence);
                String current = typing.getText().trim();
                typing.setText(current.isEmpty() ? sentence : current + " " + sentence);
                Platform.runLater(() -> {
                    typing.requestFocus();
                    typing.deselect();
                    typing.positionCaret(typing.getText().length());
                });
                SentenceHistory history = new SentenceHistory(dbManager);
                history.save(sentence, algoNames[algorithmOptions[0]]);
            } catch (Exception e) {
                System.err.println("Error generating sentence: " + e.getMessage());
            }
        });

        typing.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                String text = typing.getText().trim();
                if (text.isEmpty()) return;
                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
                updateWordBank(lastWord, suggestionFields, typing);
//                updateWordBank(lastWord, suggestionFields, typing, reportTextArea);
            }
        });

        HBox app = new HBox(left, wordBankScroll);
        app.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);

        setCenter(app);
//        setBottom(reportTextArea);
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

    public VBox createLeftVBox(Button algoButton, Button generateButton, TextArea typing) throws IOException {
        Text welcome0 = new Text("Autocomplete_");
        welcome0.getStyleClass().add("hero-text");

        Text typingLabel = new Text("Start typing to see your autocomplete suggestions");
        typingLabel.getStyleClass().add("typing-label");

        HBox buttons = new HBox(generateButton, algoButton);
        buttons.setSpacing(12);

        VBox left = new VBox(24, welcome0, typingLabel, typing, buttons);
        left.setPadding(new Insets(0, 0, 0, 72));
        left.setAlignment(Pos.CENTER_LEFT);
        return left;
    }

    public void createAlgorithmButtons(Button algoButton, Button generateButton, HBox buttons) throws IOException {
        algoButton.setPrefSize(240, 72);
        generateButton.setPrefSize(240, 72);
        buttons.setSpacing(12);
    }
    // Typing TextArea for autocompleteScene
//    public void createToImportSceneButton(Button toImportSceneButton, HelloApplication mainApp) throws IOException{
//        Color color3 = Color.web("#00000040"); //Continue Button
//        CornerRadii radii = new CornerRadii(10);
//        toImportSceneButton.setOnAction(e -> {
//            try {
//                mainApp.showUploadFilesScene();
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
//        });
//        BackgroundFill backgroundFill0 = new BackgroundFill(color3, radii, new Insets(10));
//        Background background0 = new Background(backgroundFill0);
//        toImportSceneButton.setTextFill(Color.BLACK);
//        toImportSceneButton.setBackground(background0);
//        toImportSceneButton.setPrefSize(60, 60); // sets both width and height
//
//    }
    // Updates wordbank for autocompleteScene
//    private void updateWordBank(String lastWord, Button[] suggestionFields, TextArea typing, TextArea reportTextArea) {

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
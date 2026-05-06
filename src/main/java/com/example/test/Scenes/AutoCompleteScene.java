package com.example.test.Scenes;

import com.example.test.HelloApplication;
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
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

import static java.lang.System.out;

// Sandra - Figma design, styling to JavaFx Objects, fixed alignment
// Eucharist - fixed alignment for UI, updated recentering of objects improved class design
// Kaden - created base template for JavaFx VBox/HBox

public class AutoCompleteScene extends BorderPane {
    WordService wordService;
    DatabaseManager dbManager;

    private final int suggestions = 10;
    private final String[] algoNames = {"Greedy", "Random Weighted", "Temperature", "BPE Markov"};

    public AutoCompleteScene(HelloApplication mainApp, WordService wordService, DatabaseManager dbManager) throws IOException {
        this.wordService = wordService;
        this.dbManager = dbManager;

        //Sets NavBar
        NavBar navBar = new NavBar(mainApp, "autocomplete");
        setTop(navBar);

        setAutoCompleteScene(mainApp);
    }

    public void setAutoCompleteScene(HelloApplication mainApp) throws IOException {
        setStyle("-fx-background-color: white;");

        Button[] suggestionFields = new Button[suggestions];
        //Style Algorithm buttons
        Button algoButton = new Button("Greedy");
        algoButton.getStyleClass().add("algo-button");
        algoButton.setPrefSize(240, 60);
        algoButton.setBackground(new Background(new BackgroundFill(Color.web("#D3DFFF"), new CornerRadii(10), new Insets(10))));

        //Style typing TextArea
        TextArea typing = new TextArea();
        typing.getStyleClass().add("typing-area");
        typing.setPadding(new Insets(12));

        //Create ScrollPane for wordbank
        ScrollPane wordBankScroll = createRightWordBank(suggestionFields);
        wordBankScroll.setFitToWidth(true);
        wordBankScroll.setPrefHeight(320);
        wordBankScroll.setStyle(
                "-fx-background-color: #c1c8e6;" +
                        "-fx-background: #c1c8e6;" +
                        "-fx-border-color: transparent;"
        );

        VBox left = createLeftVBox(algoButton, typing, mainApp);
//        left.setPadding(new Insets(10));

        int maxAlgo = 4;
        int[] algorithmOptions = {0};
        algoButton.setOnAction(event -> {
            algorithmOptions[0] = (algorithmOptions[0] + 1) % maxAlgo;
            algoButton.setText(algoNames[algorithmOptions[0]]);
            String text = typing.getText().trim();
            if (!text.isEmpty()) {
                String lastWord = text.contains(" ") ?
                        text.substring(text.lastIndexOf(" ") + 1) : text;
                updateWordBank(lastWord, suggestionFields, typing);
            }
        });

        typing.setOnKeyReleased(event ->{
            /**Kaden - fills the word bank with the top 3 autocomplete candidates for the last word typed */
            if(event.getCode() == KeyCode.SPACE){
                String text = typing.getText().trim();
                if(text.isEmpty()){
                    return;
                }
                //Word extraction for updating old words or adding new words
                //only update after another word is typed, provide context
                String[] sentences = text.split("\\.");
                String regex = "[,\\.\\s]"; //consider adding +

                String[] words = sentences[sentences.length - 1].trim().split(regex); //get last sentence

                if(words.length > 0){
                    try{
                        wordService.addWord(words[words.length - 1]);
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }

                    System.out.println(" added.");
                }

                if (words.length > 1){
                    System.out.println(words[words.length - 2] + words[words.length - 1]);
                    try {
                        wordService.newWord(words[words.length - 2], words[words.length - 1]);
                    } catch (Exception e) {
                        System.err.println("Error adding new word: " + e.getMessage());
                    }
                }

                if(words.length == 1){//always check on the start of a sentence
                    if(sentences.length > 1){ //PREV SENTENCE: check last word increment can end counter
                        String[] words2 = sentences[sentences.length - 2].trim().split(regex);
                        try{
                            wordService.incrementEnd(words2[words2.length - 1]);
                        }catch(Exception e){
                            System.out.println("bruh");
                        }
                    }
                    if(sentences.length > 0){ //check first word increment can end counter
                        try{
                            wordService.incrementStart(words[0]);
                        }catch(Exception e){
                            System.out.println("bruh");
                        }
                    }
                }

                //passes last word regardless of punctuation, returns '.' at the end...
                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
                updateWordBank(lastWord, suggestionFields, typing);
            }});


        setCenter(left);
        setRight(wordBankScroll);
    }
//Sets ScrollPane for word bank
    public ScrollPane createRightWordBank(Button[] suggestionFields) throws IOException {
        Text wordBankTitle = new Text("Next Word");
        wordBankTitle.getStyleClass().add("word-bank-title");

        //Create suggestion buttons for next word
        for (int i = 0; i < suggestions; i++) {
            suggestionFields[i] = new Button();
            suggestionFields[i].setVisible(false);
            suggestionFields[i].getStyleClass().add("suggestion-button");
        }
//Style wordBank
        VBox wordBank = new VBox(wordBankTitle);
        wordBank.getChildren().addAll(suggestionFields);
        wordBank.getStyleClass().add("word-bank");
        wordBank.setSpacing(12);
        wordBank.setPadding(new Insets(12));
        wordBank.setMaxHeight(Double.MAX_VALUE);

        return new ScrollPane(wordBank);
    }

//Create left VBox with text, algorithm button, and typing TextArea
    public VBox createLeftVBox(Button algoButton, TextArea typing, HelloApplication mainApp) throws IOException {
       //Set text to display
        Text welcome0 = new Text("Autocomplete");
        welcome0.getStyleClass().add("hero-text");

        Text welcome1 = new Text("_");
        welcome1.getStyleClass().add("hero-text-accent");

        TextFlow heroText = new TextFlow(welcome0, welcome1);

        Label typingLabel = new Label("Start typing to see your autocomplete suggestions");
        typingLabel.getStyleClass().add("upload-label");

        //Button container
        HBox buttons = new HBox(12, algoButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        //Create continue button to BuildSentenceScene
        Button toSentenceBuilderButton = new Button("Continue");
        toSentenceBuilderButton.getStyleClass().add("continue-button");
        toSentenceBuilderButton.setStyle("-fx-background-color: " + HelloApplication.DARKNAVY + ";");
        toSentenceBuilderButton.setOnAction(e -> mainApp.showBuildSentencesScene());

        //Set alignment for VBox
        VBox left = new VBox(24, heroText, typingLabel, buttons, typing, toSentenceBuilderButton);
        left.setPadding(new Insets(0, 72, 40, 72));
        left.setAlignment(Pos.CENTER_LEFT);
        return left;
    }

    //Create wordBank to update
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
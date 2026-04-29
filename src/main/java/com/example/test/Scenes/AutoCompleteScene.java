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
        NavBar navBar = new NavBar(mainApp);
        setTop(navBar);
        setAutoCompleteScene(mainApp);

    }
    // SETS scene for autocompleteScene
    public void setAutoCompleteScene(HelloApplication mainApp) throws IOException {
        Button[] suggestionFields = new Button[suggestions]; // change to a button instead of textfield
        Button algoButton = new Button("Algorithm: Greedy"); // expect a lot of mention of whale.
        Button generateButton = new Button("Generate Sentence");
//        Button toImportSceneButton = new Button("<-");
        TextArea typing = new TextArea();
        createTypingTextArea();
//        createToImportSceneButton(toImportSceneButton,mainApp);
        ScrollPane wordBankScroll= createRightWordBank(suggestionFields);
        wordBankScroll.setFitToWidth(true);
        wordBankScroll.setPrefHeight(320);

        VBox left = createLeftVBox(algoButton, generateButton,typing);
        left.setPadding(new Insets(10));

        TextArea reportTextArea = new TextArea();

        int maxAlgo = 4; //placeholder for number of algorithms
        int[] algorithmOptions = {0};
        algoButton.setOnAction(event ->{
            algorithmOptions[0] = (algorithmOptions[0] + 1) % maxAlgo;
            algoButton.setText("Algorithm: " + algoNames[algorithmOptions[0]]);
            String text = typing.getText().trim();
            if(!text.isEmpty()){
                String lastWord = text.contains(" ") ?
                        text.substring(text.lastIndexOf(" ") + 1) : text;
                updateWordBank(lastWord, suggestionFields, typing,reportTextArea);
            }});

        generateButton.setOnAction(event -> {
            String text = typing.getText().trim();
            try {
                // Strip trailing [END] or punctuation left over from previous generation
                String cleaned = text.replaceAll("\\s*\\[END\\]\\.?$", "").replaceAll("[.!?]+$", "").trim();

                String lastWord = cleaned.isEmpty() ? null
                        : cleaned.contains(" ") ? cleaned.substring(cleaned.lastIndexOf(" ") + 1).trim()
                        : cleaned;

                out.println("Using algorithm: " + algorithmOptions[0]);
                String sentence = new SentenceBuilder(wordService).buildSentence(lastWord, algorithmOptions[0]);
                out.println("Generated sentence: " + sentence);

                //edited so that it doesn't replace all the text
                String current = typing.getText().trim();
                typing.setText(current.isEmpty() ? sentence : current + " " + sentence);
                // moves cursor to the end
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

        typing.setOnKeyReleased(event ->{
            /** fills the word bank with the top 3 autocomplete candidates for the last word typed */
            if(event.getCode() == KeyCode.SPACE){
                String text = typing.getText().trim();
                if(text.isEmpty()){
                    return;
                }
                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
                updateWordBank(lastWord, suggestionFields, typing, reportTextArea);
            }});

        //try hbox as grid
        Label mainLabel = new Label("CS4485_Team40");
        HBox app = new HBox(left, wordBankScroll, reportTextArea, mainLabel);
        app.setAlignment(Pos.CENTER_LEFT);;

        setCenter(app);
        setRight(wordBankScroll);
        setBottom(reportTextArea);

      //  setTop(mainLabel);

        setAlignment(mainLabel, Pos.CENTER_RIGHT);
        setMargin(mainLabel, new Insets(10));

       // com.example.test.HelloApplication.autocompleteScene = new Scene(pane, 800, 320); //height, width
    }
    // Right Word Bank for autocompleteScene
    public ScrollPane createRightWordBank(Button[] suggestionFields) throws IOException {
        Text wordBankTitle = new Text(20, 100, "Next Word");
        wordBankTitle.setFont(Font.font("Verdana", 20));

        Color color2 = Color.web("#4e60ba");
        CornerRadii radii = new CornerRadii(10);

        for (int i = 0; i < suggestions; i++) {
            suggestionFields[i] = new Button();
            suggestionFields[i].setVisible(false); // hide until populated
            suggestionFields[i].setPrefWidth(160);

            BackgroundFill bf = new BackgroundFill(color2, radii, new Insets(4));
            suggestionFields[i].setBackground(new Background(bf));
            suggestionFields[i].setTextFill(Color.WHITE);
        }

        VBox wordBank = new VBox(wordBankTitle);
        wordBank.getChildren().addAll(suggestionFields);
        wordBank.setSpacing(10);
        wordBank.setPadding(new Insets(10));

        Color color = Color.web("#c1c8e6");
        BackgroundFill backgroundFill = new BackgroundFill(color, new CornerRadii(10), Insets.EMPTY);
        wordBank.setBackground(new Background(backgroundFill));

        return new ScrollPane(wordBank);
    }
    // Left VBox for autocompleteScene
    public VBox createLeftVBox(Button algoButton, Button generateButton, TextArea typing) throws IOException{
        Text welcome0 = new Text("Welcome to_");
        Text welcome1 = new Text("Sentence Builder");
        createLeftVBoxText(welcome0,welcome1);

        Text typingLabel = new Text("Start typing to see your autocomplete suggestions");

        createTextFieldTyping(typing, typingLabel);
        HBox buttons = new HBox(generateButton, algoButton);

        createAlgorithmButtons(algoButton, generateButton, buttons);

        return new VBox(welcome0, welcome1 ,typingLabel,typing,buttons);
    }
    // Left VBox text for autocompleteScene
    public void createLeftVBoxText(Text welcome0,Text welcome1) throws IOException{
        Color color2 = Color.web("#4e60ba");
        welcome0.setFont(Font.font("Verdana", 50)); // Set font family and size

        welcome1.setFont(Font.font("Verdana", 50)); // Set font family and size
        welcome1.setFill(color2);
    }
    // Typing TextField for autocompleteScene
    public void createTextFieldTyping(TextArea typing, Text typingLabel) throws IOException{

        typing.setPadding(new Insets(10)); //same as Insets(10,10,10,10)
        Color color = Color.web("#c1c8e6");
        CornerRadii radii = new CornerRadii(10);
        BackgroundFill backgroundFill = new BackgroundFill(color, radii, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        typing.setBackground(background);
        typingLabel.setFont(Font.font("Verdana", 20)); // Set font family and size

    }
    // Typing TextArea for autocompleteScene
    public void createTypingTextArea(){
        Color color = Color.web("#c1c8e6");
        CornerRadii radii = new CornerRadii(10);
        BackgroundFill backgroundFill = new BackgroundFill(color, radii, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        TextArea outputTextArea = new TextArea();
        outputTextArea.setPrefRowCount(10);
        outputTextArea.setPrefColumnCount(30);
        outputTextArea.setWrapText(true);
        outputTextArea.setPadding(new Insets(10)); //same as Insets(10,10,10,10)
        outputTextArea.setBackground(background);
    }
    // Typing TextArea for autocompleteScene
    public void createAlgorithmButtons(Button algoButton, Button generateButton, HBox buttons) throws IOException{
        Color color = Color.web("#c1c8e6");
        Color color2 = Color.web("#4e60ba");
        CornerRadii radii = new CornerRadii(10);

        //Algorithm Button
        BackgroundFill backgroundFill3 = new BackgroundFill(color, radii, new Insets(10));
        Background background3 = new Background(backgroundFill3);
        algoButton.setTextFill(color2);
        algoButton.setBackground(background3);
        algoButton.setPrefSize(200, 60); // sets both width and height

        //Generate Button
        BackgroundFill backgroundFill2 = new BackgroundFill(color2, radii, new Insets(10));
        Background background2 = new Background(backgroundFill2);
        generateButton.setTextFill(Color.WHITE);
        generateButton.setBackground(background2);
        generateButton.setPrefSize(200, 60); // sets both width and height

        buttons.setSpacing(10);
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
    private void updateWordBank(String lastWord, Button[] suggestionFields, TextArea typing, TextArea reportTextArea) {
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

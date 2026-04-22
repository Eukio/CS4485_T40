package com.example.test;

import java.io.IOException;
import static java.lang.System.out;
import java.util.Properties;

import com.example.test.backend.BPEMarkovChain;
import com.example.test.backend.BPETokenizer;
import com.example.test.backend.SentenceBuilder;
import com.example.test.backend.SentenceHistory;
import com.example.test.backend.WordService;
import com.example.test.db.DatabaseConfig;
import com.example.test.db.DatabaseManager;
import com.example.test.util.ConfigLoader;
import com.example.test.util.CorpusLoader;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class HelloApplication extends Application {
    private WordService wordService; 
    private DatabaseManager dbManager;
    private final int suggestions = 10; //placeholder for number of suggestions to show
    private final String[] algoNames = {"Greedy", "Random Weighted", "Temperature", "BPE Markov"};
    private final BPETokenizer[] tokenizer = {null};
    private final BPEMarkovChain[] bpeChain = {null};

    @Override
    public void start(Stage stage) throws IOException {

        try{
            Properties props = ConfigLoader.loadConfig();

            DatabaseConfig config = new DatabaseConfig(
                props.getProperty("db.jdbcUrl"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
            );

            this.dbManager = new DatabaseManager(config);
            wordService = new WordService(dbManager);
            new Thread(() -> {
                String corpus = CorpusLoader.loadCorpusText();
                tokenizer[0] = new BPETokenizer().train(corpus, 500);
                bpeChain[0] = new BPEMarkovChain(wordService, new SentenceBuilder(wordService));
            }).start();

        }catch (Exception e){
            System.err.println("Error initializing database connection: " + e.getMessage());
            return;
        }

        //VBOX RIGHT SIDE
        
        Text wordBankTitle = new Text(20, 100, "Next Word");
        wordBankTitle.setFont(Font.font("Verdana", 20)); // Set font family and size
        TextField[] suggestionFields = new TextField[suggestions];
        for(int i=0;i<suggestions;i++){
            suggestionFields[i] = new TextField();
            suggestionFields[i].setEditable(false);
        } 

        //word bank on right side
        VBox wordBank = new VBox(wordBankTitle);
        wordBank.getChildren().addAll(suggestionFields);
        wordBank.setSpacing(10);
        wordBank.setPadding(new Insets(10)); //Insets are just padding, can also do 4 arg
        Color color = Color.web("#c1c8e6");
        CornerRadii radii = new CornerRadii(10);
        BackgroundFill backgroundFill = new BackgroundFill(color, radii, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        wordBank.setBackground(background);
        ScrollPane wordBankScroll = new ScrollPane(wordBank);
        wordBankScroll.setFitToWidth(true);
        wordBankScroll.setPrefHeight(320);


        //VBOX LEFT SIDE
        Color color2 = Color.web("#4e60ba");
        Text welcome0 = new Text("Welcome to_");
        welcome0.setFont(Font.font("Verdana", 50)); // Set font family and size

        Text welcome1 = new Text("Sentence Builder");
        welcome1.setFont(Font.font("Verdana", 50)); // Set font family and size
        welcome1.setFill(color2);

        TextField typing = new TextField();
        typing.setPadding(new Insets(10)); //same as Insets(10,10,10,10)
        typing.setBackground(background);
        //detect if space pressed?
        // typing.setOnKeyReleased(event -> {
        //     if (event.getCode() == KeyCode.SPACE) {
        //         System.out.println(typing.getText());

        //     }
        // });
//        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode() == KeyCode.SPACE) {
//                System.out.println("Space detected globally");
//            }
//        });

        Text typingLabel = new Text("Start typing to see your autocomplete suggestions");
        typingLabel.setFont(Font.font("Verdana", 20)); // Set font family and size

        Button algoButton = new Button("Algorithm: Greedy"); // expect a lot of mention of whale.
        BackgroundFill backgroundFill3 = new BackgroundFill(color, radii, new Insets(10));
        Background background3 = new Background(backgroundFill3);
        algoButton.setTextFill(color2);
        algoButton.setBackground(background3);
        algoButton.setPrefSize(200, 60); // sets both width and height

        Button generateButton = new Button("Generate Sentence");
        BackgroundFill backgroundFill2 = new BackgroundFill(color2, radii, new Insets(10));
        Background background2 = new Background(backgroundFill2);
        generateButton.setTextFill(Color.WHITE);
        generateButton.setBackground(background2);
        generateButton.setPrefSize(200, 60); // sets both width and height
        HBox buttons = new HBox(generateButton, algoButton);
        buttons.setSpacing(10);
        VBox left = new VBox(welcome0, welcome1 ,typingLabel,typing,buttons);
        left.setPadding(new Insets(10));

        int maxAlgo = 4; //placeholder for number of algorithms 
        int[] algorithmOptions = {0};
        algoButton.setOnAction(event ->{
            /**I know we never agreed that we'd do this, but I thougth it'd be fairly easy to implement. This thing took an hour. :(
             * cycles through the available algorithms and updates the button text to show the current selection. */ 
            algorithmOptions[0] = (algorithmOptions[0] + 1) % maxAlgo;
            algoButton.setText("Algorithm: " + algoNames[algorithmOptions[0]]);
            String text = typing.getText().trim();
            if(!text.isEmpty()){
                String lastWord = text.contains(" ") ? 
                    text.substring(text.lastIndexOf(" ") + 1) : text;
                updateWordBank(lastWord, suggestionFields);
        }});

        generateButton.setOnAction(event -> {
            /** Should grab the current text from the typing field, then saves it to the sentence history */
            String text = typing.getText().trim();
            if(text.isEmpty()){
                return;
            }try{
                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
                out.println("Using algorithm: " + algorithmOptions[0]);
                String sentence = new SentenceBuilder(wordService).buildSentence(lastWord, algorithmOptions[0]);
                out.println("Generated sentence: " + sentence);
                typing.setText(sentence);
                SentenceHistory history = new SentenceHistory(dbManager);
                history.save(sentence, algoNames[algorithmOptions[0]]);
            }catch (Exception e){
                System.err.println("Error generating sentence: " + e.getMessage());
        }});

        typing.setOnKeyReleased(event ->{
            /** fills the word bank with the top 3 autocomplete candidates for the last word typed */
            if(event.getCode() == KeyCode.SPACE){
                String text = typing.getText().trim();
                if(text.isEmpty()){
                    return;
                }
                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
                updateWordBank(lastWord, suggestionFields);
        }});

        //try hbox as grid
        HBox app = new HBox(left, wordBankScroll);
        Scene scene = new Scene(app, 800, 320); //height, width
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private void updateWordBank(String lastWord, TextField[] suggestionFields){
        /** Standard issue helper function. */
        try{
            if(wordService.wordExists(lastWord)){
                long id = wordService.getWordId(lastWord);
                var candidates = wordService.getAutocompleteCandidates(id, suggestions);
                for(int i=0;i<suggestionFields.length;i++){
                    suggestionFields[i].setText(i < candidates.size() ? candidates.get(i).word() : "");
                }}}catch (Exception e){
                System.err.println("Error fetching autocomplete candidates: " + e.getMessage());
}}}

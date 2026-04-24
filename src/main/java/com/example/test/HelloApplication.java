package com.example.test;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.out;

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
import javafx.fxml.FXMLLoader;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.Group;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.*;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javafx.stage.Stage;




public class HelloApplication extends Application {

    Stage window;
    Scene importScene, wordGeneratorScene;

    private WordService wordService;
    private DatabaseManager dbManager;
    private final int suggestions = 10; //placeholder for number of suggestions to show
    private final String[] algoNames = {"Greedy", "Random Weighted", "Temperature", "BPE Markov"};
    private final BPETokenizer[] tokenizer = {null};
    private final BPEMarkovChain[] bpeChain = {null};

    public static void main(String[] args){
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        configDatabase();
        createWindow(primaryStage);
    }

    public void configDatabase(){
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

    }

    public void createWindow(Stage primaryStage) throws IOException{
        window = primaryStage;
        //Eucharist Tan
        setImportScene();
        //Kaden Chan
        setWordGeneratorScene();


        window.setScene(importScene);
        window.setTitle("Title");
        window.show();
    }

    public void setImportScene() throws IOException{
        Color color2 = Color.web("#4e60ba"); //Welcome to_
        Color color3 = Color.web("#00000040"); //Continue Button
        Color color4 = Color.web("#EDF2FF"); //Import button
        Color color5 = Color.web("#434343"); //Import button text


        Text welcome0 = new Text("Welcome to_");
        welcome0.setFont(Font.font("Verdana", 50)); // Set font family and size

        Text welcome1 = new Text("Sentence Builder");
        welcome1.setFont(Font.font("Verdana", 50)); // Set font family and size
        welcome1.setFill(color2);
        CornerRadii radii = new CornerRadii(10);

        Text uploadLabel = new Text("Upload your text file here!");
        uploadLabel.setFont(Font.font("Verdana", 20)); // Set font family and size

        //Import file Button
        Button importFileButton = new Button("Click Here");
        //importFileButton.setOnAction(e -> ); TODO: Go to Import files
        importFileButton.setTextFill(color5);

        BorderStroke outerStroke = new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1)
        );

        BorderStroke innerStroke = new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.DASHED,
                new CornerRadii(10),
                new BorderWidths(1),
                new Insets(4)
        );
        BackgroundFill backgroundFill3 = new BackgroundFill(color4, radii, new Insets(10));
        Border importBorder = new Border(outerStroke,innerStroke);
        Background background3 = new Background(backgroundFill3);

        importFileButton.setBackground(background3);
        importFileButton.setBorder(importBorder);
        importFileButton.setPrefSize(360, 60); // sets both width and height

        //Continue Button
        Button toWordGeneratorButton = new Button("Continue");
        toWordGeneratorButton.setOnAction(e -> window.setScene(wordGeneratorScene));
        BackgroundFill backgroundFill2 = new BackgroundFill(color3, radii, new Insets(10));
        Background background2 = new Background(backgroundFill2);
        toWordGeneratorButton.setTextFill(Color.BLACK);
        toWordGeneratorButton.setBackground(background2);
        toWordGeneratorButton.setPrefSize(120, 60); // sets both width and height


        VBox text = new VBox(welcome0, welcome1 ,uploadLabel, importFileButton, toWordGeneratorButton);
        text.setPadding(new Insets(10));

        Label mainLabel = new Label("CS4485_Team40");
        HBox app = new HBox(text);
        app.setSpacing(20);

        app.setAlignment(Pos.CENTER_LEFT);
        app.setTranslateX(40);
        BorderPane pane = new BorderPane();
        pane.setCenter(app);
        pane.setTop(mainLabel);

        BorderPane.setAlignment(mainLabel, Pos.CENTER_RIGHT);
        BorderPane.setMargin(mainLabel, new Insets(10));

        importScene = new Scene(pane, 800, 320);
    }
    public ScrollPane createRightWordBank(TextField[] suggestionFields) throws IOException{
        //VBOX RIGHT SIDE
        Text wordBankTitle = new Text(20, 100, "Next Word");
        wordBankTitle.setFont(Font.font("Verdana", 20)); // Set font family and size

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
        return new ScrollPane(wordBank);
    }
    public VBox createLeftVBox(Button algoButton, Button generateButton, TextField typing) throws IOException{
        Text welcome0 = new Text("Welcome to_");
        Text welcome1 = new Text("Sentence Builder");
        createLeftVBoxText(welcome0,welcome1);

        Text typingLabel = new Text("Start typing to see your autocomplete suggestions");

        createTextFieldTyping(typing, typingLabel);
        HBox buttons = new HBox(generateButton, algoButton);

        createAlgorithmButtons(algoButton, generateButton, buttons);

        return new VBox(welcome0, welcome1 ,typingLabel,typing,buttons);
    }

    public void createLeftVBoxText(Text welcome0,Text welcome1) throws IOException{
        Color color2 = Color.web("#4e60ba");
        welcome0.setFont(Font.font("Verdana", 50)); // Set font family and size

        welcome1.setFont(Font.font("Verdana", 50)); // Set font family and size
        welcome1.setFill(color2);
    }
    public void createTextFieldTyping(TextField typing, Text typingLabel) throws IOException{

        typing.setPadding(new Insets(10)); //same as Insets(10,10,10,10)
        Color color = Color.web("#c1c8e6");
        CornerRadii radii = new CornerRadii(10);
        BackgroundFill backgroundFill = new BackgroundFill(color, radii, Insets.EMPTY);
        Background background = new Background(backgroundFill);
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

        typingLabel.setFont(Font.font("Verdana", 20)); // Set font family and size

    }
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
    public TextArea createTextArea() throws IOException{
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
        return outputTextArea;
    }
    public void createToImportSceneButton(Button toImportSceneButton) throws IOException{
        Color color3 = Color.web("#00000040"); //Continue Button
        CornerRadii radii = new CornerRadii(10);
        toImportSceneButton.setOnAction(e -> window.setScene(importScene));
        BackgroundFill backgroundFill0 = new BackgroundFill(color3, radii, new Insets(10));
        Background background0 = new Background(backgroundFill0);
        toImportSceneButton.setTextFill(Color.BLACK);
        toImportSceneButton.setBackground(background0);
        toImportSceneButton.setPrefSize(60, 60); // sets both width and height

    }
    public void setWordGeneratorScene() throws IOException {
        TextField[] suggestionFields = new TextField[suggestions];
        Button algoButton = new Button("Algorithm: Greedy"); // expect a lot of mention of whale.
        Button generateButton = new Button("Generate Sentence");
        Button toImportSceneButton = new Button("<-");
        TextField typing = new TextField();
        createToImportSceneButton(toImportSceneButton);
        ScrollPane wordBankScroll= createRightWordBank(suggestionFields);
        wordBankScroll.setFitToWidth(true);
        wordBankScroll.setPrefHeight(320);

        VBox left = createLeftVBox(algoButton, generateButton,typing);
        left.setPadding(new Insets(10));

        TextArea outputTextArea = createTextArea();

        int maxAlgo = 4; //placeholder for number of algorithms
        int[] algorithmOptions = {0};
        algoButton.setOnAction(event ->{
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
        Label mainLabel = new Label("CS4485_Team40");
        HBox app = new HBox(toImportSceneButton, left, wordBankScroll, outputTextArea, mainLabel);
        app.setAlignment(Pos.CENTER_LEFT);;

        BorderPane pane = new BorderPane();
        pane.setCenter(app);
        pane.setRight(wordBankScroll);
        pane.setBottom(outputTextArea);

        pane.setTop(mainLabel);

        BorderPane.setAlignment(mainLabel, Pos.CENTER_RIGHT);
        BorderPane.setMargin(mainLabel, new Insets(10));

        wordGeneratorScene = new Scene(pane, 800, 320); //height, width
    }

    private void updateWordBank(String lastWord, TextField[] suggestionFields){
        // Standard issue helper function -Joshua John
        try{
            if(wordService.wordExists(lastWord)){
                long id = wordService.getWordId(lastWord);
                var candidates = wordService.getAutocompleteCandidates(id, suggestions);
                for(int i=0;i<suggestionFields.length;i++){
                    suggestionFields[i].setText(i < candidates.size() ? candidates.get(i).word() : "");
                }}}catch (Exception e){
                System.err.println("Error fetching autocomplete candidates: " + e.getMessage());
}}}

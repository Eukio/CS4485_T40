package com.example.test;

import java.io.IOException;
import java.util.Properties;

import com.example.test.Scenes.*;
import com.example.test.backend.BPEMarkovChain;
import com.example.test.backend.BPETokenizer;
import com.example.test.backend.SentenceBuilder;
import com.example.test.backend.WordService;

import com.example.test.db.DatabaseConfig;
import com.example.test.db.DatabaseManager;

import com.example.test.util.ConfigLoader;
import com.example.test.util.CorpusLoader;

import javafx.application.Application;

import javafx.scene.Scene;

import javafx.stage.Stage;


public class HelloApplication extends Application {

    private Stage window;
    private WordService wordService;
    private DatabaseManager dbManager;

    public static final String DARKNAVY =  "#466CCC";
    public static final String SELECTEDNAVY =  "#99AEE2";
    public static final String FILEUPLOADBLUE =  "#EDF2FF";
    public static final String LIGHTBLUE =  "#D3DFFF";
    public static final String ADDWORDGRAY = "#C5C5C5";
    public static final String TEXTGRAY =  "#434343";

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

    public void createWindow(Stage primaryStage){
        window = primaryStage;
        showHomeScene();
        window.setTitle("Sentence Builder App");
        window.show();
    }

    private void applyStylesheet(Scene scene) {
        System.out.println("Class location: " + getClass().getProtectionDomain().getCodeSource().getLocation());

        String css = getClass().getResource("/com/example/test/styles.css").toExternalForm();
//        String css = getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    // sets display for each scene
    public void showHomeScene(){
        Scene scene = new Scene(new HomeScene(this), 800, 320);
        applyStylesheet(scene);
        window.setScene(scene);
//    Scene scene = new Scene(new HomeScene(this), 800,320);
//    window.setScene(scene);
    }

    public void showUploadFilesScene() throws IOException {
        Scene scene = new Scene(new UploadFilesScene(this, window), 800, 320);
        applyStylesheet(scene);
        window.setScene(scene);
    }

    public void showAutoCompleteScene() throws IOException {
        Scene scene = new Scene(new AutoCompleteScene(this, wordService, dbManager), 800, 320);
        applyStylesheet(scene);
        window.setScene(scene);
    }

    public void showBuildSentencesScene(){
        Scene scene = new Scene(new BuildSentencesScene(this), 800, 320);
        applyStylesheet(scene);
        window.setScene(scene);
    }

    public void showReportsScene(){
        Scene scene = new Scene(new ReportsScene(this), 800, 320);
        applyStylesheet(scene);
        window.setScene(scene);
    }

//    public void setWordGeneratorScene() throws IOException {
//        TextField[] suggestionFields = new TextField[suggestions];
//        Button algoButton = new Button("Algorithm: Greedy"); // expect a lot of mention of whale.
//        Button generateButton = new Button("Generate Sentence");
//        Button toImportSceneButton = new Button("<-");
//        TextField typing = new TextField();
//        createToImportSceneButton(toImportSceneButton);
//        ScrollPane wordBankScroll= createRightWordBank(suggestionFields);
//        wordBankScroll.setFitToWidth(true);
//        wordBankScroll.setPrefHeight(320);
//
//        VBox left = createLeftVBox(algoButton, generateButton,typing);
//        left.setPadding(new Insets(10));
//
//        TextArea outputTextArea = createTextArea();
//
//        int maxAlgo = 4; //placeholder for number of algorithms
//        int[] algorithmOptions = {0};
//        algoButton.setOnAction(event ->{
//            algorithmOptions[0] = (algorithmOptions[0] + 1) % maxAlgo;
//            algoButton.setText("Algorithm: " + algoNames[algorithmOptions[0]]);
//            String text = typing.getText().trim();
//            if(!text.isEmpty()){
//                String lastWord = text.contains(" ") ?
//                    text.substring(text.lastIndexOf(" ") + 1) : text;
//                updateWordBank(lastWord, suggestionFields);
//        }});
//
//        generateButton.setOnAction(event -> {
//            /** Should grab the current text from the typing field, then saves it to the sentence history */
//            String text = typing.getText().trim();
//            if(text.isEmpty()){
//                return;
//            }try{
//                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
//                out.println("Using algorithm: " + algorithmOptions[0]);
//                String sentence = new SentenceBuilder(wordService).buildSentence(lastWord, algorithmOptions[0]);
//                out.println("Generated sentence: " + sentence);
//                typing.setText(sentence);
//                SentenceHistory history = new SentenceHistory(dbManager);
//                history.save(sentence, algoNames[algorithmOptions[0]]);
//            }catch (Exception e){
//                System.err.println("Error generating sentence: " + e.getMessage());
//        }});
//
//        typing.setOnKeyReleased(event ->{
//            /** fills the word bank with the top 3 autocomplete candidates for the last word typed */
//            if(event.getCode() == KeyCode.SPACE){
//                String text = typing.getText().trim();
//                if(text.isEmpty()){
//                    return;
//                }
//                //Word extraction for updating old words or adding new words
//                //only update after another word is typed, provide context
//                String regex = "[,\\.\\s]";
//                String[] words = text.split(regex);
//                System.out.println(words.length);
//                if (words.length > 1){
//                    System.out.println(words[words.length - 2] + words[words.length - 1]);
//                    try{
//                        wordService.newWord(words[words.length - 2], words[words.length - 1]);
//                    }
//                    catch(Exception e){
//                        System.err.println("Error adding new word: " + e.getMessage());
//                    }
//                }
//                String lastWord = text.contains(" ") ? text.substring(text.lastIndexOf(" ") + 1).trim() : text;
//                updateWordBank(lastWord, suggestionFields);
//        }});
//
//        //try hbox as grid
//        Label mainLabel = new Label("CS4485_Team40");
//        HBox app = new HBox(toImportSceneButton, left, wordBankScroll, outputTextArea, mainLabel);
//        app.setAlignment(Pos.CENTER_LEFT);;
//
//}


}

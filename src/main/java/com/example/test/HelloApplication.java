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

    public WordService getWordService() {
        return wordService;
    }

    public DatabaseManager getDbManager() {
        return dbManager;
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
    public void showHomeScene() {
        Scene scene = new Scene(new HomeScene(this), 1200, 700);
        applyStylesheet(scene);
        window.setScene(scene);
    }

    public void showUploadFilesScene() throws IOException {
        Scene scene = new Scene(new UploadFilesScene(this, window), 1200, 700);
        applyStylesheet(scene);
        window.setScene(scene);
    }

    public void showAutoCompleteScene() throws IOException {
        Scene scene = new Scene(new AutoCompleteScene(this, wordService, dbManager), 1200, 700);
        applyStylesheet(scene);
        window.setScene(scene);
    }

    public void showBuildSentencesScene() {
        Scene scene = new Scene(new BuildSentencesScene(this), 1200, 700);
        applyStylesheet(scene);
        window.setScene(scene);
    }

    public void showReportsScene() {
        Scene scene = new Scene(new ReportsScene(this), 1200, 700);
        applyStylesheet(scene);
        window.setScene(scene);
    }
}

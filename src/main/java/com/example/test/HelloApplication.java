package com.example.test;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Properties;

import static java.lang.System.out;

import com.example.test.Scenes.*;
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
import javafx.geometry.Pos;

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

import javafx.application.Platform;


import javafx.stage.FileChooser;
import java.io.File;
import com.example.test.service.BookFolderImporter;


public class HelloApplication extends Application {

    private Stage window;
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

    public void createWindow(Stage primaryStage){
        window = primaryStage;
        showHomeScene();
        window.setTitle("Sentence Builder App");
        window.show();
    }
    // SETS the display for HomeScene
    public void showHomeScene(){
    Scene scene = new Scene(new HomeScene(this), 800,320);
    window.setScene(scene);
    }
    public void showUploadFilesScene() throws IOException {
        Scene scene = new Scene(new UploadFilesScene(this, window), 800, 320);
        window.setScene(scene);
    }
    public void showAutoCompleteScene() throws IOException {
        Scene scene = new Scene(new AutoCompleteScene(this, wordService, dbManager), 800, 320);
        window.setScene(scene);
    }
    public void showBuildSentencesScene(){
        Scene scene = new Scene(new BuildSentencesScene(this), 800, 320);
        window.setScene(scene);
    }
    public void showReportsScene(){
        Scene scene = new Scene(new ReportsScene(this), 800, 320);
        window.setScene(scene);
    }

}

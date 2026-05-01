package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.example.test.db.DatabaseManager;

import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import java.sql.SQLException;

import java.util.Properties;

import com.example.test.db.DatabaseConfig;
import com.example.test.db.DatabaseManager;
import com.example.test.util.ConfigLoader;

import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;
import javafx.geometry.Pos;

public class ReportsScene extends BorderPane {

    /*
     * This text is updated when the user searches for a word.
     * It must be a class variable so searchHBox() can change it.
     */
    private Text wordDetailsText;

    //change between ascending and descending alpha
    private boolean isAscendingA = true;

    //change between ascending and descending frequency
    private boolean isAscendingF = true;
    /*
     * This text is updated when the user clicks Alpha or Freq.
     * It must be a class variable so the buttons can change it.
     */
    private Text wordsText;

    public ReportsScene(HelloApplication mainApp) {
        NavBar navBar = new NavBar(mainApp, "reports");
        setTop(navBar);
        setReportScenePage();
    }

    /*
     * Builds the full Reports page.
     */
    public void setReportScenePage() {
        Text welcomeLine = new Text("Reports");
        welcomeLine.getStyleClass().add("hero-text");

        Text welcomeAccent = new Text("_");
        welcomeAccent.getStyleClass().add("hero-text-accent");

        TextFlow heroText = new TextFlow(welcomeLine, welcomeAccent);

        Label subtitleLabel = new Label("Search and explore word data from the database");
        subtitleLabel.getStyleClass().add("upload-label");

        VBox reportSceneBox = new VBox(24, heroText, subtitleLabel, reportContainer());
        reportSceneBox.setPadding(new Insets(0, 72, 40, 72));
        reportSceneBox.setAlignment(Pos.CENTER_LEFT);

        setCenter(reportSceneBox);
        setStyle("-fx-background-color: white;");
    }
    /*
     * Main container:
     * left side = search + word details
     * right side = word bank
     */
    public HBox reportContainer() {
        VBox left = leftSearchVBox();
        VBox right = rightWordBankBox();

        HBox.setHgrow(left, Priority.ALWAYS);

        HBox reportContainer = new HBox(29, left, right);
        reportContainer.setMaxWidth(Double.MAX_VALUE);
        reportContainer.setPrefSize(321, 470);
        reportContainer.setPadding(new Insets(20));

        reportContainer.setStyle(
                "-fx-background-color: " + HelloApplication.LIGHTBLUE + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;"
        );

        return reportContainer;
    }

    /*
     * Left side of the page.
     */
    public VBox leftSearchVBox() {
        Text searchWordText = new Text("Search Word");
        searchWordText.getStyleClass().add("subtitle-text");

        VBox leftSearchBox = new VBox(searchWordText, searchHBox(), wordDetailsBox());
        leftSearchBox.setSpacing(20);

        return leftSearchBox;
    }

    /*
     * Search bar.
     *
     * User types a word, then either:
     * - clicks the button
     * - presses Enter
     *
     * Then we call DatabaseManager.printWordDetails(word).
     */
    public HBox searchHBox() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search Word Here");
        searchField.getStyleClass().add("subtitle-text");
        searchField.setStyle("-fx-background-color: transparent;");

        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = new Button("o");
        searchButton.getStyleClass().add("search-button");

        searchButton.setOnAction(e -> {
            String word = searchField.getText().trim();

            if (word.isEmpty()) {
                wordDetailsText.setText("Please enter a word first.");
                return;
            }

            try {
                Properties props = ConfigLoader.loadConfig();

                DatabaseConfig config = new DatabaseConfig(
                        props.getProperty("db.jdbcUrl"),
                        props.getProperty("db.username"),
                        props.getProperty("db.password")
                );

                /*
                 * Open database connection, get details, then close connection.
                 */
                try (DatabaseManager db = new DatabaseManager(config)) {
                    wordDetailsText.setText(db.printWordDetails(word));
                }

            } catch (Exception ex) {
                wordDetailsText.setText("Could not load word details: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        /*
         * Pressing Enter in the text field acts like clicking search.
         */
        searchField.setOnAction(e -> searchButton.fire());

        HBox searchBox = new HBox(searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(0, 10, 0, 10));
        searchBox.setPrefHeight(77);

        searchBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;"
        );

        return searchBox;
    }

    /*
     * Word details card.
     * This is updated after searching for a word.
     */
    public VBox wordDetailsBox() {

        Text wordDetailsTitle = new Text("Word Details");
        wordDetailsTitle.getStyleClass().add("subtitle-text");

        // This is the dynamic text that updates after search
        wordDetailsText = new Text("Search a word to see details.");
        wordDetailsText.getStyleClass().add("subtitle-text");
        wordDetailsText.setStyle("-fx-font-size: 14px; -fx-font-weight: normal;");

        //text inside a VBox (this is the scroll content)
        VBox content = new VBox(wordDetailsText);
        content.setPadding(new Insets(10));
        content.setSpacing(10);

        //Create scroll pane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPrefHeight(250); // controls visible height

        // Styling so it looks clean
        scrollPane.setStyle(
                "-fx-background: white;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: transparent;" +
                        "-fx-viewport-fill: white;"
        );

        // Outer box (title + scroll area)
        VBox box = new VBox(10, wordDetailsTitle, scrollPane);
        box.setPadding(new Insets(10));

        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;"
        );

        box.setPrefSize(386, 285);

        return box;
    }

    /*
     * Right side of the page.
     */
    public VBox rightWordBankBox() {
        VBox rightWordBox = new VBox(sortButtonContainer(), wordBankPane());
        rightWordBox.setPrefSize(300, 260);
        rightWordBox.setPadding(new Insets(10));
        rightWordBox.setSpacing(10);

        rightWordBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;"
        );

        return rightWordBox;
    }

    /*
     * Alpha and Freq buttons.
     *
     * Alpha calls getAllWordsAlpha().
     * Freq calls getAllWordsFrequency().
     */
    public HBox sortButtonContainer() {
        Button alphabeticalButton = new Button("Alpha");
        Button frequencyButton = new Button("Freq");

        alphabeticalButton.getStyleClass().add("button");
        frequencyButton.getStyleClass().add("button");

        // remove all the inline setStyle calls — handled by CSS

        alphabeticalButton.setOnAction(e -> {
            try {
                Properties props = ConfigLoader.loadConfig();
                DatabaseConfig config = new DatabaseConfig(
                        props.getProperty("db.jdbcUrl"),
                        props.getProperty("db.username"),
                        props.getProperty("db.password")
                );
                try (DatabaseManager db = new DatabaseManager(config)) {
                    wordsText.setText(isAscendingA ? db.getAllWordsAlphaASC() : db.getAllWordsAlphaDESC());
                    isAscendingA = !isAscendingA;
                }
            } catch (Exception ex) {
                wordsText.setText("Could not load words alphabetically: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        frequencyButton.setOnAction(e -> {
            try {
                Properties props = ConfigLoader.loadConfig();
                DatabaseConfig config = new DatabaseConfig(
                        props.getProperty("db.jdbcUrl"),
                        props.getProperty("db.username"),
                        props.getProperty("db.password")
                );
                try (DatabaseManager db = new DatabaseManager(config)) {
                    wordsText.setText(isAscendingF ? db.getAllWordsFrequencyASC() : db.getAllWordsFrequencyDESC());
                    isAscendingF = !isAscendingF;
                }
            } catch (Exception ex) {
                wordsText.setText("Could not load words by frequency: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        return new HBox(12, alphabeticalButton, frequencyButton);
    }
    /*
     * Scrollable word bank.
     * The text changes when Alpha or Freq is clicked.
     */
    public ScrollPane wordBankPane() {
        wordsText = new Text("Click Alpha or Freq to load words.");
        wordsText.getStyleClass().add("subtitle-text");
        wordsText.setStyle("-fx-font-size: 14px; -fx-font-weight: normal;");

        VBox fileDetailsBox = new VBox(10, wordsText);
        fileDetailsBox.setPadding(new Insets(10));
        fileDetailsBox.setStyle("-fx-background-color: white;");

        ScrollPane scrollBox = new ScrollPane(fileDetailsBox);
        scrollBox.setPrefViewportHeight(345);
        scrollBox.setFitToWidth(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        scrollBox.setStyle(
                "-fx-background: white;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: transparent;" +
                        "-fx-viewport-fill: white;"
        );

        return scrollBox;
    }
}
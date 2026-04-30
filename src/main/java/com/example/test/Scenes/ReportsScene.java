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

public class ReportsScene extends BorderPane {
    public ReportsScene(HelloApplication mainApp){
        NavBar navBar = new NavBar(mainApp);
        setTop(navBar);
        setReportScenePage();

    }
    public void setReportScenePage() {
        Text welcomeLine = new Text("Reports_\n");
        welcomeLine.getStyleClass().add("hero-text");

        VBox reportSceneBox = new VBox(  welcomeLine, reportContainer());
        reportSceneBox.setPadding(new Insets(20, 20, 20, 20));
        reportSceneBox.setSpacing(20);

        setCenter(reportSceneBox);

    }

    public HBox reportContainer() {
        VBox left = leftSearchVBox();
        VBox right = rightWordBankBox();

        HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);

        HBox reportContainer = new HBox(29, left, right);

        reportContainer.setMaxWidth(Double.MAX_VALUE);

        reportContainer.setPrefSize(321, 470);
        reportContainer.setStyle("-fx-background-color: " + HelloApplication.LIGHTBLUE + ";" +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; ");
        reportContainer.setPadding(new Insets(20));

        return reportContainer;
    }

    //TODO: Link SearchBar with Word Details LEFTSIDE
    public VBox leftSearchVBox(){
        Text searchWordText = new Text("Search Word");
        searchWordText.getStyleClass().add("subtitle-text");

        HBox searchBox = searchHBox();
        VBox wordDetailsBox = wordDetailsBox();

        VBox leftSearchBox = new VBox(searchWordText, searchBox, wordDetailsBox);
        leftSearchBox.setSpacing(20);
        return leftSearchBox;
    }

    public HBox searchHBox(){
        // TODO: Link searchField
        TextField searchField = new TextField("");
        searchField.setPromptText("Search Word Here");
        searchField.getStyleClass().add("subtitle-text");

        searchField.setStyle("-fx-background-color: transparent;");

        HBox.setHgrow(searchField, javafx.scene.layout.Priority.ALWAYS);

        // SearchButton does not have an Icon yet
        Button searchButton = new Button("o");
        searchButton.setPrefSize(44, 44);

        // SearchBox and styling
        HBox searchBox = new HBox(searchField, searchButton);

        searchBox.setAlignment(javafx.geometry.Pos.CENTER);
        searchBox.setPadding(new Insets(0, 10, 0, 10)); // 10px padding on the sides

        searchBox.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; ");

        searchBox.setPrefHeight(77);

        return searchBox;
    }

    public VBox wordDetailsBox(){
        Text wordDetailsTitle = new Text("Word Details");
        wordDetailsTitle.getStyleClass().add("subtitle-text");

        //TODO: Word Details Text HERE
        Text wordDetailsText = new Text("Here is all the word Details Here!");
        wordDetailsTitle.getStyleClass().add("subtitle-text");
        wordDetailsText.getStyleClass().add("subtitle-text");
        wordDetailsText.setStyle("-fx-font-size: 14px; -fx-font-weight: normal;");

        VBox wordDetailsBox = new VBox(20, wordDetailsTitle,wordDetailsText);
        wordDetailsBox.setPadding(new Insets(10));

        wordDetailsBox.setStyle("-fx-background-color: white;"+ "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; ");
        wordDetailsBox.setPrefSize(386,285);

        return wordDetailsBox;

    }
    //TODO: Link buttons to show words within the database RIGHT SIDE
    public VBox rightWordBankBox(){
        ScrollPane wordBankPane = wordBankPane();
        HBox sortButtonContainer = sortButtonContainer();


        VBox rightWordBox = new VBox(sortButtonContainer, wordBankPane);
        rightWordBox.setStyle("-fx-background-color: white;"+ "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; ");
        rightWordBox.setPrefSize(300,260);
        rightWordBox.setPadding(new Insets(10));
        rightWordBox.setSpacing(10);

        return rightWordBox;
    }
    public HBox sortButtonContainer(){
        //TODO: Buttons to sort from Database
        Button alphabeticalButton = new Button("Alpha");
        Button frequencyButton = new Button("Freq");

        // Button styling
        alphabeticalButton.getStyleClass().add("suggestion-button");
        frequencyButton.getStyleClass().add("suggestion-button");
        alphabeticalButton.setStyle("-fx-background-color: " + HelloApplication.LIGHTBLUE + ";" +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; ");
        frequencyButton.setStyle("-fx-background-color: " + HelloApplication.LIGHTBLUE + ";" +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; ");
        return new HBox(20, alphabeticalButton,frequencyButton);
    }
    public ScrollPane wordBankPane() {
        //TODO: Text of the words in the database
        Text words = new Text("Word");

        VBox fileDetailsBox = new VBox(10, words);
        words.getStyleClass().add("subtitle-text");
        words.setStyle("-fx-font-size: 14px; -fx-font-weight: normal;");
        fileDetailsBox.setPadding(new Insets(10));

        fileDetailsBox.setStyle("-fx-background-color: white;");

        // Create ScrollPane
        ScrollPane scrollBox = new ScrollPane(fileDetailsBox);
        scrollBox.setPrefViewportHeight(345);

        scrollBox.setStyle(
                "-fx-background: white; " +
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: transparent; " +
                        "-fx-viewport-fill: white;"
        );

        scrollBox.setFitToWidth(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return scrollBox;
    }

}

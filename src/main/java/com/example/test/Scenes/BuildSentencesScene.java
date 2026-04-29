package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class BuildSentencesScene extends BorderPane {
    public BuildSentencesScene(HelloApplication mainApp){
    Button toHomeScreenButton = new Button("Go to Home");
        toHomeScreenButton.setOnAction(e -> {
       mainApp.showHomeScene();
    });
    getChildren().add(toHomeScreenButton);
}}

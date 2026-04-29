package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class BuildSentencesScene extends BorderPane {
    public BuildSentencesScene(HelloApplication mainApp) {
        NavBar navBar = new NavBar(mainApp);
        setTop(navBar);
    }

}

package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class ReportsScene extends BorderPane {
    public ReportsScene(HelloApplication mainApp){
        NavBar navBar = new NavBar(mainApp, "reports");
        setTop(navBar);
    }}

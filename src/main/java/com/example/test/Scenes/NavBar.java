package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class NavBar extends HBox {
//Eucharist - UI Navigation Bar that allows user to switch between scenes, is an HBox that is passed between each scene class

    private Button activeButton = null;

    public NavBar(HelloApplication mainApp, String activeScene) {
        Button home         = createNavButton("Home");
        Button upload       = createNavButton("Upload Files");
        Button autocomplete = createNavButton("Autocomplete");
        Button build        = createNavButton("Build Sentences");
        Button reports      = createNavButton("Reports");

        setStyle("-fx-background-color: " + HelloApplication.DARKNAVY + ";");
        setPrefHeight(48);

        // Sandra - Set active based on which scene we're on
        switch (activeScene) {
            case "upload"       -> setActive(upload);
            case "autocomplete" -> setActive(autocomplete);
            case "build"        -> setActive(build);
            case "reports"      -> setActive(reports);
            default             -> setActive(home);
        }

        home.setOnAction(e -> {
            setActive(home); mainApp.showHomeScene();
        });

        upload.setOnAction(e -> {
            setActive(upload);
            try { mainApp.showUploadFilesScene(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        autocomplete.setOnAction(e -> {
            setActive(autocomplete);
            try { mainApp.showAutoCompleteScene(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        build.setOnAction(e -> { setActive(build); mainApp.showBuildSentencesScene(); });

        reports.setOnAction(e -> { setActive(reports); mainApp.showReportsScene(); });

        getChildren().addAll(home, upload, autocomplete, build, reports);
    }

    private Button createNavButton(String label) {
        Button btn = new Button(label);
        btn.getStyleClass().add("nav-button");
        return btn;
    }

    private void setActive(Button btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
            if (!activeButton.getStyleClass().contains("nav-button"))
                activeButton.getStyleClass().add("nav-button");
        }
        btn.getStyleClass().remove("nav-button");
        btn.getStyleClass().add("nav-button-active");
        activeButton = btn;
    }
}
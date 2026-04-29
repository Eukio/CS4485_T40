package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class NavBar extends HBox {
    public NavBar(HelloApplication mainApp){
        Button home = new Button("Home");
        Button upload = new Button("Upload Files");
        Button autocomplete = new Button("Autocomplete");
        Button build = new Button("Build Sentences");
        Button reports = new Button("Reports");
        setSpacing(10);
        setPadding(new Insets(10));

        home.setOnAction(e -> mainApp.showHomeScene());

        upload.setOnAction(e -> {
            try {
                mainApp.showUploadFilesScene();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        autocomplete.setOnAction(e -> {
            try {
                mainApp.showAutoCompleteScene();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        build.setOnAction(e -> mainApp.showBuildSentencesScene());
        reports.setOnAction(e -> mainApp.showReportsScene());
        getChildren().addAll(home, upload, autocomplete, build, reports);
    }
}

package com.example.test.Scenes;

import com.example.test.HelloApplication;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class HomeScene extends BorderPane {
    public HomeScene(HelloApplication mainApp){
        Button toUploadFilesSceneButton = new Button("Go to Scene 2");
        toUploadFilesSceneButton.setOnAction(e -> {
            try {
                mainApp.showUploadFilesScene();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        setCenter(toUploadFilesSceneButton);
    }
}

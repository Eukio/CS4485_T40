package com.example.test.Scenes;

import com.example.test.HelloApplication;
import com.example.test.db.DatabaseConfig;
import com.example.test.db.DatabaseManager;
import com.example.test.service.BookFolderImporter;
import com.example.test.util.ConfigLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class UploadFilesScene extends BorderPane{
    private Stage window;
    public UploadFilesScene(HelloApplication mainApp, Stage window) throws IOException {
        this.window = window;
        NavBar navBar = new NavBar(mainApp);
        setTop(navBar);
        setUploadFilesScene(mainApp);

    }

    // SETS the display for uploadFilesScene
    public void setUploadFilesScene(HelloApplication mainApp) throws IOException {

        Text welcome0 = new Text("Welcome to_");
        welcome0.getStyleClass().add("hero-text");

        Text welcome1 = new Text("Sentence Builder");
        welcome1.getStyleClass().add("hero-text-accent");

        Label uploadLabel = new Label("Upload your text file here!");
        uploadLabel.getStyleClass().add("upload-label");

        Button importFileButton = new Button("Click Here");

        BorderStroke outerStroke = new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1)
        );
        BorderStroke innerStroke = new BorderStroke(
                Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(10), new BorderWidths(1), new Insets(4)
        );
        importFileButton.setBorder(new Border(outerStroke, innerStroke));
        importFileButton.setBackground(new Background(new BackgroundFill(Color.web("#EDF2FF"), new CornerRadii(10), new Insets(10))));
        importFileButton.setPrefSize(360, 60);

        importFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose a text file");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File selectedFile = fileChooser.showOpenDialog(window);
            if (selectedFile != null) {
                try {
                    Properties props = ConfigLoader.loadConfig();
                    DatabaseConfig config = new DatabaseConfig(
                            props.getProperty("db.jdbcUrl"),
                            props.getProperty("db.username"),
                            props.getProperty("db.password")
                    );
                    boolean skipAlready = Boolean.parseBoolean(props.getProperty("db.skipAlready"));
                    try (DatabaseManager db = new DatabaseManager(config)) {
                        new BookFolderImporter(db, null).importFile(selectedFile.toPath(), skipAlready);
                    }
                    uploadLabel.setText("File imported successfully!");
                } catch (Exception ex) {
                    uploadLabel.setText("Import failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        Button toWordGeneratorButton = new Button("Continue");
        toWordGeneratorButton.getStyleClass().add("continue-button");
        toWordGeneratorButton.setOnAction(e -> {
            try {
                mainApp.showAutoCompleteScene();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        //TODO: Christian's button
        Button showFileDetailsButton = new Button("File Info");
        showFileDetailsButton.getStyleClass().add("continue-button");

        HBox buttonContainer = new HBox(10, toWordGeneratorButton, showFileDetailsButton);
        VBox box = new VBox(10, welcome0, welcome1, uploadLabel, importFileButton, buttonContainer);
        box.setPadding(new Insets(10));

        ScrollPane fileDetailsPane = fileDetailsPane();
        setRight(fileDetailsPane);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox app = new HBox(box,spacer, fileDetailsPane);
        app.setSpacing(20);
        app.setAlignment(Pos.CENTER_LEFT);
        app.setPadding(new Insets(0, 20, 0, 40));

        setCenter(app);
    }
    //TODO: Christian's scrollPane
    public ScrollPane fileDetailsPane(){
        Text fileTitleText = new Text(10, 100, "File Details");
        fileTitleText.getStyleClass().add("subtitle-text");
            Text fileDetailsText = new Text("This is the file details here...");
            fileDetailsText.setWrappingWidth(280);
        VBox fileDetailsBox = new VBox(fileTitleText);
        fileDetailsBox.getChildren().add(fileDetailsText);
        ScrollPane scrollBox = new ScrollPane(fileDetailsBox);
        scrollBox.setPrefViewportWidth(300);
        scrollBox.setPrefViewportHeight(100);
        scrollBox.setFitToWidth(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollBox.setPrefHeight(320);
        return scrollBox;
    }
}

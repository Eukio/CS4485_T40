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


//Christian Verderame


public class UploadFilesScene extends BorderPane {

    private Stage window;

    //  class-level so button can update it
    private Text fileDetailsText;

    public UploadFilesScene(HelloApplication mainApp, Stage window) throws IOException {
        this.window = window;

        NavBar navBar = new NavBar(mainApp);
        setTop(navBar);

        setUploadFilesScene(mainApp);
    }

    //Christian Verderame
    /**
        upload scenes for the file insertion and file history page

     */
    public void setUploadFilesScene(HelloApplication mainApp) throws IOException {

        Text welcome0 = new Text("Welcome to_");
        welcome0.getStyleClass().add("hero-text");

        Text welcome1 = new Text("Sentence Builder");
        welcome1.getStyleClass().add("hero-text-accent");

        Label uploadLabel = new Label("Upload your text file here!");
        uploadLabel.getStyleClass().add("upload-label");

        // ================= FILE IMPORT BUTTON =================
        Button importFileButton = new Button("Click Here");

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

                    boolean skipAlready = Boolean.parseBoolean(
                            props.getProperty("db.skipAlready")
                    );

                    try (DatabaseManager db = new DatabaseManager(config)) {
                        new BookFolderImporter(db, null)
                                .importFile(selectedFile.toPath(), skipAlready);
                    }

                    uploadLabel.setText("File imported successfully!");

                } catch (Exception ex) {
                    uploadLabel.setText("Import failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // ================= NAV BUTTON =================
        Button toWordGeneratorButton = new Button("Continue");
        toWordGeneratorButton.getStyleClass().add("continue-button");

        toWordGeneratorButton.setOnAction(e -> {
            try {
                mainApp.showAutoCompleteScene();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // ================= FILE INFO BUTTON =================
        Button showFileDetailsButton = new Button("File Info");
        showFileDetailsButton.getStyleClass().add("continue-button");

        // THIS IS THE IMPORTANT PART
        showFileDetailsButton.setOnAction(e -> {
            try {
                Properties props = ConfigLoader.loadConfig();

                DatabaseConfig config = new DatabaseConfig(
                        props.getProperty("db.jdbcUrl"),
                        props.getProperty("db.username"),
                        props.getProperty("db.password")
                );

                try (DatabaseManager db = new DatabaseManager(config)) {
                    fileDetailsText.setText(db.getAllFilesStatsString());
                }

            } catch (Exception ex) {
                fileDetailsText.setText("Could not load file details: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ================= LAYOUT =================
        HBox buttonContainer = new HBox(10, toWordGeneratorButton, showFileDetailsButton);

        VBox box = new VBox(
                10,
                welcome0,
                welcome1,
                uploadLabel,
                importFileButton,
                buttonContainer
        );

        box.setPadding(new Insets(10));

        // create pane AFTER field exists
        ScrollPane fileDetailsPane = fileDetailsPane();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox app = new HBox(box, spacer, fileDetailsPane);
        app.setSpacing(20);
        app.setAlignment(Pos.CENTER_LEFT);
        app.setPadding(new Insets(0, 20, 0, 40));

        setCenter(app);
    }

    // ================= RIGHT PANEL =================
    public ScrollPane fileDetailsPane() {

        Text fileTitleText = new Text("File Details");
        fileTitleText.getStyleClass().add("subtitle-text");

        //assign to class field
        fileDetailsText = new Text("Click 'File Info' to view imported file data.");
        fileDetailsText.setWrappingWidth(280);

        VBox fileDetailsBox = new VBox(10, fileTitleText, fileDetailsText);
        fileDetailsBox.setPadding(new Insets(10));

        ScrollPane scrollBox = new ScrollPane(fileDetailsBox);
        scrollBox.setPrefViewportWidth(300);
        scrollBox.setPrefViewportHeight(320);
        scrollBox.setFitToWidth(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        return scrollBox;
    }
}

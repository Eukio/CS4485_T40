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
import javafx.scene.text.TextFlow;

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

        // ================= HERO TEXT =================
        Text welcome0 = new Text("Upload ");
        welcome0.getStyleClass().add("hero-text");

        Text welcome1 = new Text("Files_");
        welcome1.getStyleClass().add("hero-text-accent");

        TextFlow heroText = new TextFlow(welcome0, welcome1);

        Label uploadLabel = new Label("Upload a text file here to add it to our database");
        uploadLabel.getStyleClass().add("upload-label");

        // ================= FILE IMPORT BUTTON =================
        Button importFileButton = new Button("Click Here");
        BorderStroke outerStroke = new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1)
        );
        BorderStroke innerStroke = new BorderStroke(
                Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(10), new BorderWidths(1), new Insets(4)
        );
        importFileButton.setBorder(new Border(outerStroke, innerStroke));
        importFileButton.setBackground(new Background(new BackgroundFill(Color.web("#EDF2FF"), new CornerRadii(10), new Insets(10))));
        importFileButton.setPrefSize(760, 60);

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

        // ================= SHOW DUPLICATES BUTTON =================
        Button showDuplicatesButton = new Button("Show Duplicates");
        showDuplicatesButton.getStyleClass().add("button");

        // ================= FILE INFO BUTTON =================
        Button showFileDetailsButton = new Button("File Info");
        showFileDetailsButton.getStyleClass().add("button");

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

        // ================= CENTER CONTENT =================
        HBox buttonRow = new HBox(12, showDuplicatesButton, showFileDetailsButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        VBox centerBox = new VBox(24, heroText, uploadLabel, importFileButton, buttonRow);
        centerBox.setPadding(new Insets(0, 0, 0, 72));
        centerBox.setAlignment(Pos.CENTER_LEFT);

// ================= RIGHT PANEL =================
        ScrollPane fileDetailsPane = fileDetailsPane();

// ================= NAV BUTTON bottom right =================
        Button toWordGeneratorButton = new Button("Go to Autocomplete");
        toWordGeneratorButton.getStyleClass().add("button");
        toWordGeneratorButton.setOnAction(e -> {
            try {
                mainApp.showAutoCompleteScene();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        VBox bottomBox = new VBox(toWordGeneratorButton);
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.setPadding(new Insets(0, 24, 24, 0));

        setCenter(centerBox);
        setRight(fileDetailsPane);
        setBottom(bottomBox);
        setStyle("-fx-background-color: white;");
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

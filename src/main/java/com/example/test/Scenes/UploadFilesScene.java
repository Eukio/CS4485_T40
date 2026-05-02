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

        NavBar navBar = new NavBar(mainApp, "upload");
        setTop(navBar);

        setUploadFilesScene(mainApp);
    }

    //Christian Verderame
    /**
        upload scenes for the file insertion and file history page

     */
    public void setUploadFilesScene(HelloApplication mainApp) throws IOException {
        setStyle("-fx-background-color: white;");

        // Replace the two Text nodes and the VBox with:
        Text welcome0 = new Text("Upload ");
        welcome0.getStyleClass().add("hero-text");

        Text welcome1 = new Text("Files_");
        welcome1.getStyleClass().add("hero-text-accent");

        TextFlow heroText = new TextFlow(welcome0, welcome1);

        Label uploadLabel = new Label("Upload your text file here!");
        uploadLabel.getStyleClass().add("upload-label");

        // ================= FILE IMPORT BUTTON =================
        Button importFileButton = new Button("Click Here");
        importFileButton.setPrefWidth(520);
        importFileButton.setStyle("-fx-background-radius: 10px;");

        BorderStroke outerStroke = new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1)
        );
        BorderStroke innerStroke = new BorderStroke(
                Color.BLACK, BorderStrokeStyle.DASHED, new CornerRadii(10), new BorderWidths(1), new Insets(4)
        );
        importFileButton.setBorder(new Border(outerStroke, innerStroke));


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
        // ================= SHOW DUPLICATES BUTTON =================
        //TODO: Christian here is your button
        Button showDuplicatesButton = new Button("Show Duplicates");

        showDuplicatesButton.setOnAction(e -> {
            try {
                Properties props = ConfigLoader.loadConfig();

                DatabaseConfig config = new DatabaseConfig(
                        props.getProperty("db.jdbcUrl"),
                        props.getProperty("db.username"),
                        props.getProperty("db.password")
                );

                try (DatabaseManager db = new DatabaseManager(config)) {
                    fileDetailsText.setText(db.getDuplicateFileNamesString());
                }

            } catch (Exception ex) {
                fileDetailsText.setText("Could not load duplicate file names: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ================= NAV BUTTON =================
        Button toWordGeneratorButton = new Button("Continue");
        toWordGeneratorButton.getStyleClass().add("continue-button");
        toWordGeneratorButton.setStyle("-fx-background-color: " + HelloApplication.DARKNAVY + ";");


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
        HBox buttonContainer = new HBox(10, toWordGeneratorButton, showDuplicatesButton, showFileDetailsButton);

        VBox box = new VBox(
                10,
                heroText,
                uploadLabel,
                importFileButton,
                buttonContainer
        );

        box.setPadding(new Insets(10));

        // create pane AFTER field exists
        ScrollPane fileDetailsPane = fileDetailsPane();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //center the left vbox
        VBox centerWrapper = new VBox(box);
        centerWrapper.setAlignment(Pos.CENTER_LEFT);
        centerWrapper.setPadding(new Insets(0, 20, 0, 72));

        setCenter(centerWrapper);
        setRight(fileDetailsPane);
    }

    // ================= RIGHT PANEL =================
    public ScrollPane fileDetailsPane() {

        Text fileTitleText = new Text("File Details");
        fileTitleText.getStyleClass().add("subtitle-text");

        fileDetailsText = new Text("Click 'File Info' to view imported file data.");
        fileDetailsText.setWrappingWidth(280);
        fileDetailsText.setStyle("-fx-fill: #1a1a1a; -fx-font-family: 'Verdana'; -fx-font-size: 14px;");

        VBox fileDetailsBox = new VBox(10, fileTitleText, fileDetailsText);
        fileDetailsBox.setPadding(new Insets(10));

        ScrollPane scrollBox = new ScrollPane(fileDetailsBox);
        scrollBox.setPrefViewportWidth(300);
        scrollBox.setPrefViewportHeight(320);
        scrollBox.setFitToWidth(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background: white;"
        );

        // Wrap in a panel styled like the history box
        HBox header = new HBox(fileTitleText);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(4, 8, 4, 8));

        VBox panel = new VBox(4, header, scrollBox);
        panel.setPadding(new Insets(10));
        panel.setStyle(
                "-fx-background-color: #D3DFFF;" +
                        "-fx-background-radius: 12;"
        );

        // Wrap panel in a ScrollPane to return correct type
        ScrollPane wrapper = new ScrollPane(panel);
        wrapper.setFitToWidth(true);
        wrapper.setFitToHeight(true); // fills available height
        wrapper.setPrefViewportWidth(320);
        wrapper.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );

        scrollBox.setFitToHeight(true);
        VBox.setVgrow(scrollBox, Priority.ALWAYS);
        panel.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(panel, Priority.ALWAYS);

        return wrapper;
    }
}

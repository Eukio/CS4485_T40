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

        VBox box = new VBox(welcome0, welcome1, uploadLabel, importFileButton, toWordGeneratorButton);
        box.setPadding(new Insets(10));

        Label mainLabel = new Label("CS4485_Team40");
        HBox app = new HBox(box);
        app.setSpacing(20);
        app.setAlignment(Pos.CENTER_LEFT);
        app.setTranslateX(40);

        setCenter(app);
        setAlignment(mainLabel, Pos.CENTER_RIGHT);
        setMargin(mainLabel, new Insets(10));
    }

}

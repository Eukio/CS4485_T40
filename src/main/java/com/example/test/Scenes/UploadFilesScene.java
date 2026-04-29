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
        setUploadFilesScene(mainApp);

    }

    // SETS the display for uploadFilesScene
    public void setUploadFilesScene(HelloApplication mainApp) throws IOException {
        Color color2 = Color.web("#4e60ba"); //Welcome to_
        Color color3 = Color.web("#00000040"); //Continue Button
        Color color4 = Color.web("#EDF2FF"); //Import button
        Color color5 = Color.web("#434343"); //Import button text


        Text welcome0 = new Text("Welcome to_");
        welcome0.setFont(Font.font("Verdana", 50)); // Set font family and size

        Text welcome1 = new Text("Sentence Builder");
        welcome1.setFont(Font.font("Verdana", 50)); // Set font family and size
        welcome1.setFill(color2);
        CornerRadii radii = new CornerRadii(10);

        //Text uploadLabel = new Text("Upload your text file here!");
        Label uploadLabel = new Label("Upload your text file here!");
        uploadLabel.setFont(Font.font("Verdana", 20)); // Set font family and size

        //Import file Button
        Button importFileButton = new Button("Click Here");
        //importFileButton.setOnAction(e -> ); TODO: Go to Import files
        //importFileButton.setTextFill(color5);

        //Christian verderame
        //feature to import txt files from users into the database
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

                    String jdbcUrl = props.getProperty("db.jdbcUrl");
                    String username = props.getProperty("db.username");
                    String password = props.getProperty("db.password");
                    boolean skipAlready = Boolean.parseBoolean(props.getProperty("db.skipAlready"));

                    DatabaseConfig config = new DatabaseConfig(jdbcUrl, username, password);

                    try (DatabaseManager db = new DatabaseManager(config)) {
                        BookFolderImporter importer = new BookFolderImporter(db, null);

                        importer.importFile(selectedFile.toPath(), skipAlready);
                    }

                    uploadLabel.setText("File imported successfully!");

                } catch (Exception ex) {
                    uploadLabel.setText("Import failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });


        BorderStroke outerStroke = new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1)
        );

        BorderStroke innerStroke = new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.DASHED,
                new CornerRadii(10),
                new BorderWidths(1),
                new Insets(4)
        );
        BackgroundFill backgroundFill3 = new BackgroundFill(color4, radii, new Insets(10));
        Border importBorder = new Border(outerStroke,innerStroke);
        Background background3 = new Background(backgroundFill3);

        importFileButton.setBackground(background3);
        importFileButton.setBorder(importBorder);
        importFileButton.setPrefSize(360, 60); // sets both width and height

        //Continue Button
        Button toWordGeneratorButton = new Button("Continue");
        toWordGeneratorButton.setOnAction(e -> {
            try {
                mainApp.showAutoCompleteScene();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        BackgroundFill backgroundFill2 = new BackgroundFill(color3, radii, new Insets(10));
        Background background2 = new Background(backgroundFill2);
        toWordGeneratorButton.setTextFill(Color.BLACK);
        toWordGeneratorButton.setBackground(background2);
        toWordGeneratorButton.setPrefSize(120, 60); // sets both width and height


        VBox box = new VBox(welcome0, welcome1 ,uploadLabel, importFileButton, toWordGeneratorButton);
        box.setPadding(new Insets(10));

        Label mainLabel = new Label("CS4485_Team40");
        HBox app = new HBox(box);
        app.setSpacing(20);

        app.setAlignment(Pos.CENTER_LEFT);
        app.setTranslateX(40);
        setCenter(app);
        setTop(mainLabel);
        setAlignment(mainLabel, Pos.CENTER_RIGHT);
        setMargin(mainLabel, new Insets(10));

    }
}

package com.example.test;

import java.io.IOException;
import java.util.Properties;

import com.example.test.backend.WordService;
import com.example.test.db.DatabaseConfig;
import com.example.test.db.DatabaseManager;
import com.example.test.util.ConfigLoader;

import com.example.test.backend.GeneratedSentence;
import com.example.test.backend.SentenceBuilder;
import com.example.test.backend.SentenceHistory;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class HelloApplication extends Application {
    private WordService wordService; 
    

    @Override
    public void start(Stage stage) throws IOException {

        try{
            Properties props = ConfigLoader.loadConfig();

            DatabaseConfig config = new DatabaseConfig(
                props.getProperty("db.jdbcUrl"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
            );

            DatabaseManager dbManager = new DatabaseManager(config);
            wordService = new WordService(dbManager);

        }catch (Exception e){
            System.err.println("Error initializing database connection: " + e.getMessage());
            return;
        }

        //VBOX RIGHT SIDE
        
        Text wordBankTitle = new Text(20, 100, "Next Word");
        wordBankTitle.setFont(Font.font("Verdana", 20)); // Set font family and size
        TextField textField0 = new TextField();
        textField0.setEditable(false);
        TextField textField1 = new TextField();
        textField1.setEditable(false);
        TextField textField2 = new TextField();
        textField2.setEditable(false);

        //word bank on right side
        VBox wordBank = new VBox(wordBankTitle, textField0, textField1,textField2);
        wordBank.setSpacing(10);
        wordBank.setPadding(new Insets(10)); //Insets are just padding, can also do 4 arg
        Color color = Color.web("#c1c8e6");
        CornerRadii radii = new CornerRadii(10);
        BackgroundFill backgroundFill = new BackgroundFill(color, radii, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        wordBank.setBackground(background);

        //VBOX LEFT SIDE
        Color color2 = Color.web("#4e60ba");
        Text welcome0 = new Text("Welcome to_");
        welcome0.setFont(Font.font("Verdana", 50)); // Set font family and size

        Text welcome1 = new Text("Sentence Builder");
        welcome1.setFont(Font.font("Verdana", 50)); // Set font family and size
        welcome1.setFill(color2);

        TextField typing = new TextField();
        typing.setPadding(new Insets(10)); //same as Insets(10,10,10,10)
        typing.setBackground(background);
        //detect if space pressed?
        typing.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                System.out.println(typing.getText());

            }
        });
//        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode() == KeyCode.SPACE) {
//                System.out.println("Space detected globally");
//            }
//        });

        Text typingLabel = new Text("Start typing to see your autocomplete suggestions");
        typingLabel.setFont(Font.font("Verdana", 20)); // Set font family and size

        Button generateButton = new Button("Generate Sentence");
        BackgroundFill backgroundFill2 = new BackgroundFill(color2, radii, new Insets(10));
        Background background2 = new Background(backgroundFill2);
        generateButton.setTextFill(Color.WHITE);
        generateButton.setBackground(background2);
        generateButton.setPrefSize(200, 60); // sets both width and height
        VBox left = new VBox(welcome0, welcome1 ,typingLabel,typing,generateButton);
        left.setPadding(new Insets(10));

        









        //try hbox as grid
        HBox app = new HBox(left, wordBank);
        Scene scene = new Scene(app, 800, 320); //height, width
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}

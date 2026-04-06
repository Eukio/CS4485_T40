package com.example.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;


import java.io.IOException;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {


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

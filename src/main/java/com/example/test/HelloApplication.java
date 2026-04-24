package com.example.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

//how do we want to organize module to control FE -> BE data pass
//import com.example.test.backend.WordService;

import javax.swing.text.Position;
import java.io.IOException;


public class HelloApplication extends Application {

    Stage window;
    Scene importScene, wordGeneratorScene;
    public static void main(String[] args){
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws IOException {

        window = primaryStage;
        //Eucharist Tan
        setImportScene();
        //Kaden Chan
        setWordGeneratorScene();


        window.setScene(importScene);
        window.setTitle("Title");
        window.show();

    }
    public void setImportScene() throws IOException{
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

        Text uploadLabel = new Text("Upload your text file here!");
        uploadLabel.setFont(Font.font("Verdana", 20)); // Set font family and size

        //Import file Button
        Button importFileButton = new Button("Click Here");
        //importFileButton.setOnAction(e -> ); TODO: Go to Import files
        importFileButton.setTextFill(color5);

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
        toWordGeneratorButton.setOnAction(e -> window.setScene(wordGeneratorScene));
        BackgroundFill backgroundFill2 = new BackgroundFill(color3, radii, new Insets(10));
        Background background2 = new Background(backgroundFill2);
        toWordGeneratorButton.setTextFill(Color.BLACK);
        toWordGeneratorButton.setBackground(background2);
        toWordGeneratorButton.setPrefSize(120, 60); // sets both width and height


        VBox text = new VBox(welcome0, welcome1 ,uploadLabel, importFileButton, toWordGeneratorButton);
        text.setPadding(new Insets(10));

        Label mainLabel = new Label("CS4485_Team40");
        HBox app = new HBox(text);
        app.setSpacing(20);

        app.setAlignment(Pos.CENTER_LEFT);
        app.setTranslateX(40);
        BorderPane pane = new BorderPane();
        pane.setCenter(app);
        pane.setTop(mainLabel);

        BorderPane.setAlignment(mainLabel, Pos.CENTER_RIGHT);
        BorderPane.setMargin(mainLabel, new Insets(10));

        importScene = new Scene(pane, 800, 320);
    }
    public void setWordGeneratorScene() throws IOException {
        Button toImportSceneButton = new Button("<-");
        Color color3 = Color.web("#00000040"); //Continue Button
        CornerRadii radii = new CornerRadii(10);
        toImportSceneButton.setOnAction(e -> window.setScene(importScene));
        BackgroundFill backgroundFill0 = new BackgroundFill(color3, radii, new Insets(10));
        Background background0 = new Background(backgroundFill0);
        toImportSceneButton.setTextFill(Color.BLACK);
        toImportSceneButton.setBackground(background0);
        toImportSceneButton.setPrefSize(60, 60); // sets both width and height

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
        wordBank.setTranslateX(-10);

        Color color = Color.web("#c1c8e6");
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

        //TEXTAREA
        TextArea outputTextArea = new TextArea();
        outputTextArea.setPrefRowCount(10);
        outputTextArea.setPrefColumnCount(30);
        outputTextArea.setWrapText(true);
        outputTextArea.setPadding(new Insets(10)); //same as Insets(10,10,10,10)
        outputTextArea.setBackground(background);

        //try hbox as grid
        Label mainLabel = new Label("CS4485_Team40");
        HBox app = new HBox(toImportSceneButton, left, wordBank, outputTextArea, mainLabel);
        app.setAlignment(Pos.CENTER_LEFT);;

        BorderPane pane = new BorderPane();
        pane.setCenter(app);
        pane.setRight(wordBank);
        pane.setBottom(outputTextArea);

        pane.setTop(mainLabel);

        BorderPane.setAlignment(mainLabel, Pos.CENTER_RIGHT);
        BorderPane.setMargin(mainLabel, new Insets(10));

        wordGeneratorScene = new Scene(pane, 800, 320); //height, width
    }
}

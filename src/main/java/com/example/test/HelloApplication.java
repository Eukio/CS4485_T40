package com.example.test;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Color accentColor = Color.web("#4e60ba");
        Color panelColor  = Color.web("#c1c8e6");
        Color hoverColor  = Color.web("#a0aad4");
        CornerRadii radii = new CornerRadii(10);
        Background panelBg  = new Background(new BackgroundFill(panelColor, radii, Insets.EMPTY));
        Background accentBg = new Background(new BackgroundFill(accentColor, radii, new Insets(10)));
        Background hoverBg  = new Background(new BackgroundFill(hoverColor, radii, Insets.EMPTY));

        // ── RIGHT PANEL: Word Bank ──────────────────────────────────────────
        Text wordBankTitle = new Text("Next Word");
        wordBankTitle.fontProperty().bind(
                Bindings.createObjectBinding(
                        () -> Font.font("Verdana", stage.getWidth() * 0.025),
                        stage.widthProperty()
                )
        );

        // Declare wordBank first so buttons can bind to its width
        VBox wordBank = new VBox(10, wordBankTitle);
        wordBank.setPadding(new Insets(10));
        wordBank.setBackground(panelBg);
        wordBank.prefWidthProperty().bind(stage.widthProperty().multiply(0.25));

        // Build 10 word bank buttons
        Button[] wordButtons = new Button[10];
        for (int i = 0; i < wordButtons.length; i++) {
            Button btn = new Button("");
            btn.setBackground(panelBg);
            btn.setTextFill(Color.BLACK);
            btn.prefWidthProperty().bind(wordBank.widthProperty().subtract(20));

            // Hover effect
            btn.setOnMouseEntered(e -> btn.setBackground(hoverBg));
            btn.setOnMouseExited(e -> btn.setBackground(panelBg));

            // Click: append the word to the typing field (wired up after typing is declared)
            wordButtons[i] = btn;
        }
        wordBank.getChildren().addAll(wordButtons);

        // ── LEFT PANEL: Main Content ────────────────────────────────────────
        Text welcome0 = new Text("Welcome to_");
        welcome0.fontProperty().bind(
                Bindings.createObjectBinding(
                        () -> Font.font("Verdana", stage.getWidth() * 0.0625),
                        stage.widthProperty()
                )
        );

        Text welcome1 = new Text("Sentence Builder");
        welcome1.fontProperty().bind(
                Bindings.createObjectBinding(
                        () -> Font.font("Verdana", stage.getWidth() * 0.0625),
                        stage.widthProperty()
                )
        );
        welcome1.setFill(accentColor);

        Text typingLabel = new Text("Start typing to see your autocomplete suggestions");
        typingLabel.fontProperty().bind(
                Bindings.createObjectBinding(
                        () -> Font.font("Verdana", stage.getWidth() * 0.025),
                        stage.widthProperty()
                )
        );

        // text area for writing input
        TextArea typing = new TextArea();
        typing.setBackground(panelBg);
        typing.setPadding(new Insets(10));
        typing.setWrapText(true);          // wrap instead of scroll horizontally
        typing.setPrefRowCount(1);         // start at ~3 lines tall
        typing.setMinHeight(60);
        HBox.setHgrow(typing, Priority.ALWAYS);
        VBox.setVgrow(typing, Priority.ALWAYS);  // stretches vertically as VBox grows

        typing.setStyle(
                "-fx-control-inner-background: #c1c8e6;" +  // fill color matches panelBg
                        "-fx-background-color: transparent;" +           // outer background
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-family: Verdana;" +                // match the rest of the app
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10;"
        );

        // Font size scales with window width like the other text elements
//        typing.fontProperty().bind(
//                Bindings.createObjectBinding(
//                        () -> Font.font("Verdana", stage.getWidth() * 0.018),
//                        stage.widthProperty()
//                )
//        );

        // Wire up button clicks now that `typing` exists
        for (Button btn : wordButtons) {
            btn.setOnAction(e -> {
                String word = btn.getText();
                if (!word.isEmpty()) {
                    String current = typing.getText();
                    // Add a space before the word if there's already text
                    if (!current.isEmpty() && !current.endsWith(" ")) {
                        typing.setText(current + " " + word + " ");
                    } else {
                        typing.setText(current + word + " ");
                    }
                    //move the cursor to the end of the line
                    typing.requestFocus();
                    javafx.application.Platform.runLater(() ->
                            typing.positionCaret(typing.getText().length())
                    );
                }
            });
        }

        // Key handler: on SPACE or PERIOD, push the last typed word into the bank
        typing.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.PERIOD || event.getCode() == KeyCode.ENTER) {
                String fullText = typing.getText().trim().replaceAll("[. ]+$", "");
                String[] tokens = fullText.split("\\s+");
                String word = tokens[tokens.length - 1].replace(".", "");

                if (!word.isEmpty()) {
                    System.out.println("Completed word: " + word);

                    // Shift all buttons down, put new word at top
                    for (int i = wordButtons.length - 1; i > 0; i--) {
                        wordButtons[i].setText(wordButtons[i - 1].getText());
                    }
                    wordButtons[0].setText(word);
                }
            }
        });

        Button generateButton = new Button("Generate Sentence");
        generateButton.setTextFill(Color.WHITE);
        generateButton.setBackground(accentBg);
        generateButton.prefWidthProperty().bind(stage.widthProperty().multiply(0.25));
        generateButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.18));

        VBox left = new VBox(5, welcome0, welcome1, typingLabel, typing, generateButton);
        left.setPadding(new Insets(5));
        HBox.setHgrow(left, Priority.ALWAYS);

        // ── ROOT ────────────────────────────────────────────────────────────
        HBox app = new HBox(10, left, wordBank);
        app.setPadding(new Insets(10));

        Scene scene = new Scene(app, 800, 320);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
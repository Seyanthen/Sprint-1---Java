package com.gradebook;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Minimal JavaFX application used to verify that JavaFX launches correctly.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label message = new Label("JavaFX launched successfully");
        Scene scene = new Scene(message, 400, 200);

        stage.setTitle("Gradebook System - JavaFX Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

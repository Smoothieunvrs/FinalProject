package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Stage primaryStage;  // Hold the reference to the primary stage

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;  // Initialize the primary stage

        // Initially load the first scene (e.g., hello-view.fxml)
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("DashboardEnroll.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        //stage.setTitle("JavaFX + MySQL App");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // Method to change the scene
    public static void changeScene(String fxml) throws IOException {
        // Load the new FXML file for the scene
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Scene newScene = new Scene(fxmlLoader.load());
        // Set the new scene on the existing primary stage
        primaryStage.setScene(newScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/TelaInicial.fxml")));
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("BioMeasure");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

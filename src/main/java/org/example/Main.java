package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/TelaInicial.fxml"));
        Scene scene = new Scene(root, 600, 400);
        //scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("BioMeasure");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // tela cheia
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

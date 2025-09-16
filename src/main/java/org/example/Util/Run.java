package org.example.Util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

import static javafx.application.Application.launch;

public class Run extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/TelaInicial.fxml")));
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("BioMeasure");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {

            System.out.println(e);
        }

    }

    public static void main(String[] args) {
        System.out.println("Teste");
        launch();
    }
}

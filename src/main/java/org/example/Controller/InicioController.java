package org.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class InicioController {

    @FXML
    private void abrirLogin(ActionEvent event) {
        trocarCena("/Login.fxml", "Login - BioMeasure");
    }

    @FXML
    private void abrirCadastro(ActionEvent event) {
        trocarCena("/Cadastro.fxml", "Cadastro - BioMeasure");
    }



    private void trocarCena(String caminhoFxml, String tituloJanela) {
        try {
            Stage stage = (Stage) Stage.getWindows().filtered(Window::isShowing).getFirst();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(caminhoFxml)));
            stage.setScene(new Scene(root));
            stage.setTitle(tituloJanela);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

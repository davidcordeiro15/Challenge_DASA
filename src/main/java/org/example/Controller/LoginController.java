package org.example.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import org.example.FileSelector;
import org.example.Model.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

    private static final List<Usuario> usuarios = new ArrayList<>();

    @FXML private TextField campoEmail;
    @FXML private PasswordField campoSenha;

    @FXML
    private void realizarLogin() {
        String email = campoEmail.getText();
        String senha = campoSenha.getText();

        if (email.equals("admin@biomeasure.com") && senha.equals("1234")) {
            abrirVisualizador3D();
            fecharTelaAtual();
        } else {
            mostrarAlerta("Credenciais inválidas. Verifique seu email e senha.", Alert.AlertType.ERROR);
        }
    }

    private void abrirVisualizador3D() {
        // Abre diretamente o seletor de arquivo e inicia visualização 3D
        FileSelector seletor = new FileSelector();
        seletor.showDisplay();
    }

    @FXML
    private void abrirCadastro() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Cadastro.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Cadastro");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao abrir tela de cadastro.", Alert.AlertType.ERROR);
        }
    }

    private void fecharTelaAtual() {
        Stage stage = (Stage) campoEmail.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Mensagem");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    public static void adicionarUsuario(Usuario u) {
        usuarios.add(u);
    }
}

package org.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.Usuario;
import org.example.Service.UsuarioService;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    private UsuarioService usuarioService = new UsuarioService();

    private Visualizador3DController visualizador = new Visualizador3DController();

    // 🔹 Ação do botão "Entrar"
    @FXML
    private void realizarLogin(ActionEvent event) {
        String email = campoEmail.getText();
        String senha = campoSenha.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            mostrarAlerta("Erro", "Preencha todos os campos!", Alert.AlertType.WARNING);
            return;
        }

        try {
            Usuario usuario = usuarioService.autenticarUsuario(senha, email);

            if (usuario != null && usuario.getId() > 0) {
                mostrarAlerta("Sucesso", "Login realizado com sucesso!", Alert.AlertType.INFORMATION);
                visualizador.abrirSeletorArquivo();

            } else {
                mostrarAlerta("Erro", "Credenciais inválidas!", Alert.AlertType.ERROR);
            }

        } catch (SQLException ex) {
            mostrarAlerta("Erro de banco de dados", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // 🔹 Ação do botão "Cadastrar-se"
    @FXML
    private void abrirCadastro(ActionEvent event) {

        try {
            Stage stage = (Stage) campoEmail.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cadastro.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - BioMeasure");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Método auxiliar para mostrar alertas
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}

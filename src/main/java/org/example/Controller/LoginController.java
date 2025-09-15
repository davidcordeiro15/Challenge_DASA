package org.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.Model.Usuario;
import org.example.Service.UsuarioService;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    private UsuarioService usuarioService = new UsuarioService();

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
        mostrarAlerta("Cadastro", "Abrir tela de cadastro aqui...", Alert.AlertType.INFORMATION);
        // Aqui você pode carregar outro FXML para cadastro
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

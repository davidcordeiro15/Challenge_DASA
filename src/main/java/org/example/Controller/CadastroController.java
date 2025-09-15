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

public class CadastroController {

    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    private UsuarioService usuarioService = new UsuarioService();

    // 🔹 Ação do botão "Cadastrar"
    @FXML
    private void cadastrar(ActionEvent event) {
        String nome = campoNome.getText();
        String email = campoEmail.getText();
        String senha = campoSenha.getText();

        // Validação simples dos campos
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            mostrarAlerta("Erro", "Todos os campos são obrigatórios!", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Criar usuário
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(senha);

            Usuario novoUsuario = usuarioService.adicionarUsuario(usuario);

            if (novoUsuario != null) {
                mostrarAlerta("Sucesso", "Usuário cadastrado com sucesso!", Alert.AlertType.INFORMATION);

                // Limpar campos
                campoNome.clear();
                campoEmail.clear();
                campoSenha.clear();


            } else {
                mostrarAlerta("Erro", "Email já está em uso!", Alert.AlertType.ERROR);
            }

        } catch (SQLException ex) {
            mostrarAlerta("Erro de banco de dados", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // 🔹 Método auxiliar para exibir alertas
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    @FXML
    private void voltarLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) campoNome.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/View/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - BioMeasure");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

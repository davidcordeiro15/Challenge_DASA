package org.example.Controller;



import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.Model.Usuario;

public class CadastroController {
    @FXML private TextField campoNome;
    @FXML private TextField campoEmail;
    @FXML private PasswordField campoSenha;

    public void cadastrar() {
        String nome = campoNome.getText();
        String email = campoEmail.getText();
        String senha = campoSenha.getText();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Preencha todos os campos!").showAndWait();
            return;
        }

        Usuario novo = new Usuario(nome, email, senha);
        LoginController.adicionarUsuario(novo);
        new Alert(Alert.AlertType.INFORMATION, "Usuário cadastrado!").showAndWait();

        campoNome.clear();
        campoEmail.clear();
        campoSenha.clear();
    }
}


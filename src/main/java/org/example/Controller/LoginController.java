package org.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Model.Usuario;
import org.example.Service.UsuarioService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    private UsuarioService usuarioService = new UsuarioService();

    // Caminho do arquivo OBJ selecionado
    private static String selectedObjPath = null;

    // 🔹 Método getter para o caminho do arquivo (usado pelo ViewerController)
    public static String getSelectedObjPath() {
        return selectedObjPath;
    }

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
                // Executar na thread do JavaFX
                javafx.application.Platform.runLater(() -> {
                    mostrarAlerta("Sucesso", "Login realizado com sucesso!", Alert.AlertType.INFORMATION);

                    // Fechar a janela atual de login
                    Stage stage = (Stage) campoEmail.getScene().getWindow();
                    stage.close();

                    // 1️⃣ PRIMEIRO: Abrir seletor de arquivo OBJ
                    String objPath = abrirSeletorOBJ(null);

                    if (objPath != null && !objPath.isEmpty()) {
                        // Armazenar o caminho selecionado
                        selectedObjPath = objPath;

                        // 2️⃣ SEGUNDO: Abrir a Tela3D com o modelo
                        abrirTela3D(objPath);
                    } else {
                        mostrarAlerta("Cancelado", "Nenhum arquivo foi selecionado. O login foi cancelado.", Alert.AlertType.WARNING);
                        // Reabrir a tela de login se nenhum arquivo foi selecionado
                        reabrirLogin();
                    }
                });

            } else {
                mostrarAlerta("Erro", "Credenciais inválidas!", Alert.AlertType.ERROR);
            }

        } catch (SQLException ex) {
            javafx.application.Platform.runLater(() -> {
                mostrarAlerta("Erro de banco de dados", ex.getMessage(), Alert.AlertType.ERROR);
            });
        } catch (Exception ex) {
            javafx.application.Platform.runLater(() -> {
                mostrarAlerta("Erro", "Ocorreu um erro inesperado: " + ex.getMessage(), Alert.AlertType.ERROR);
            });
            ex.printStackTrace();
        }
    }

    /**
     * Abre o seletor de arquivos OBJ
     */
    private String abrirSeletorOBJ(Stage parentStage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar Modelo 3D para Análise");

            // Configurar filtros de arquivo
            FileChooser.ExtensionFilter objFilter = new FileChooser.ExtensionFilter(
                    "Arquivos OBJ (*.obj)", "*.obj"
            );
            FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter(
                    "Todos os arquivos (*.*)", "*.*"
            );

            fileChooser.getExtensionFilters().addAll(objFilter, allFilter);
            fileChooser.setSelectedExtensionFilter(objFilter);

            // Definir diretório inicial (se existir)
            File initialDir = new File(System.getProperty("user.home"));
            if (initialDir.exists()) {
                fileChooser.setInitialDirectory(initialDir);
            }

            // Mostrar dialog de seleção (sem parent stage se for null)
            File selectedFile = fileChooser.showOpenDialog(parentStage);

            if (selectedFile != null && selectedFile.exists()) {
                return selectedFile.getAbsolutePath();
            }

        } catch (Exception e) {
            javafx.application.Platform.runLater(() -> {
                mostrarAlerta("Erro", "Erro ao abrir seletor de arquivos: " + e.getMessage(), Alert.AlertType.ERROR);
            });
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Abre a Tela3D.fxml com o modelo já selecionado
     */
    private void abrirTela3D(String objPath) {
        try {
            // Carregar o FXML da Tela3D
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tela3D.fxml"));
            Parent root = loader.load();

            // Obter o controller da Tela3D
            ViewerController viewerController = loader.getController();

            // Carregar o modelo OBJ automaticamente
            if (viewerController != null) {
                viewerController.loadModelOnStartup(objPath);
            }

            // Criar nova cena e stage
            Scene scene = new Scene(root);
            Stage newStage = new Stage();

            newStage.setTitle("BioMeasure 3D - Visualizador Médico - " + new File(objPath).getName());
            newStage.setScene(scene);
            newStage.setMaximized(true); // Abrir maximizado para melhor visualização

            // Mostrar a janela
            newStage.show();

            // Focar na área 3D para receber eventos de teclado
            javafx.application.Platform.runLater(() -> {
                root.requestFocus();
            });

        } catch (IOException e) {
            javafx.application.Platform.runLater(() -> {
                mostrarAlerta("Erro", "Erro ao carregar a interface 3D: " + e.getMessage(), Alert.AlertType.ERROR);
            });
            e.printStackTrace();
        } catch (Exception e) {
            javafx.application.Platform.runLater(() -> {
                mostrarAlerta("Erro", "Erro inesperado ao abrir visualizador 3D: " + e.getMessage(), Alert.AlertType.ERROR);
            });
            e.printStackTrace();
        }
    }

    /**
     * Reabre a tela de login caso o usuário cancele a seleção de arquivo
     */
    private void reabrirLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setTitle("Login - BioMeasure");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            javafx.application.Platform.runLater(() -> {
                mostrarAlerta("Erro", "Erro ao reabrir tela de login: " + e.getMessage(), Alert.AlertType.ERROR);
            });
            e.printStackTrace();
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
            stage.setTitle("Cadastro - BioMeasure");
            stage.show();

        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao abrir tela de cadastro: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // 🔹 Método auxiliar para mostrar alertas (sempre na thread do JavaFX)
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        // Garantir que está na thread do JavaFX
        if (javafx.application.Platform.isFxApplicationThread()) {
            Alert alerta = new Alert(tipo);
            alerta.setTitle(titulo);
            alerta.setHeaderText(null);
            alerta.setContentText(mensagem);
            alerta.showAndWait();
        } else {
            javafx.application.Platform.runLater(() -> {
                Alert alerta = new Alert(tipo);
                alerta.setTitle(titulo);
                alerta.setHeaderText(null);
                alerta.setContentText(mensagem);
                alerta.showAndWait();
            });
        }
    }
}
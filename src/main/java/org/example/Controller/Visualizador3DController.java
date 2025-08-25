package org.example.Controller;


import javafx.fxml.FXML;
import org.example.FileSelector;

public class Visualizador3DController {

    @FXML
    private void abrirSeletorArquivo() {
        FileSelector seletor = new FileSelector();
        seletor.showDisplay();
    }
}
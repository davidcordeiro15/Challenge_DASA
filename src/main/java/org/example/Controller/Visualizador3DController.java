package org.example.Controller;


import javafx.fxml.FXML;
import org.example.Ui.FileSelector;
import org.example.Ui.GLBFileViewer;

public class Visualizador3DController {

    @FXML
    public void    abrirSeletorArquivo() {
        FileSelector seletor = new FileSelector();
        String path = seletor.showDisplay();
        if (!path.isEmpty()) {
            GLBFileViewer.setGlbFilePath(path);
        }

    }
}
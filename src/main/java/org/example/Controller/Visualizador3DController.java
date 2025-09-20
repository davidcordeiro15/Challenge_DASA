package org.example.Controller;


import javafx.fxml.FXML;
import org.example.Ui.FileSelector;
import org.example.Ui.OBJFileViewer;
import org.example.Util.OBJLoader;

public class Visualizador3DController {

    @FXML
    public void abrirSeletorArquivo() {
        FileSelector seletor = new FileSelector();
        String path = seletor.showDisplay();

        if (path != null && !path.isEmpty()) {
            OBJFileViewer.abrirComArquivo(path);
        }
    }

}
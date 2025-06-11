package org.example;

import javax.swing.*;
import java.io.File;

public class FileSelector {

    // Método que mostra a tela para escolher o arquivo .glb
    public void showDisplay() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo .glb");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.getName().toLowerCase().endsWith(".glb")) {
                JOptionPane.showMessageDialog(null, "Selecione apenas arquivos .glb", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }


            GLBFileViewer.setGlbFilePath(selectedFile.getAbsolutePath());
            GLBFileViewer app = new GLBFileViewer();
            app.start();
        } else {
            System.out.println("Nenhum arquivo selecionado.");
        }
    }
}


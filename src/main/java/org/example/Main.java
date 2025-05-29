package org.example;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo .glb");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.getName().toLowerCase().endsWith(".glb")) {
                JOptionPane.showMessageDialog(null, "Selecione apenas arquivos .glb", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Passa o caminho do .glb para a classe de visualização
            GLBFileViewer.setGlbFilePath(selectedFile.getAbsolutePath());
            GLBFileViewer app = new GLBFileViewer();
            app.start(); // inicia o visualizador
        } else {
            System.out.println("Nenhum arquivo selecionado.");
        }

    }
}

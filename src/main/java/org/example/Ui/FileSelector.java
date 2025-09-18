package org.example.Ui;

import javax.swing.*;
import java.io.File;

public class FileSelector {

    public String showDisplay() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo .obj");

        // Filtro para arquivos GLB
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
            }

            @Override
            public String getDescription() {
                return "Arquivos OBJ (*.obj)";
            }
        });

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        }

        return null;
    }
}
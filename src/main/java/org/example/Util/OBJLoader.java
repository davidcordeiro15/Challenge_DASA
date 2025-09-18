package org.example.Util;

import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import javafx.scene.Node;
import javafx.scene.transform.Scale;

public class OBJLoader {

    public static Node loadOBJ(String filePath) throws Exception {
        ObjModelImporter importer = new ObjModelImporter();
        importer.read(filePath);

        Node[] nodes = importer.getImport();
        importer.close();

        if (nodes.length == 0) {
            throw new Exception("Erro: Nenhum nó foi carregado do arquivo OBJ.");
        }

        Node model = nodes[0];

        // Ajustar escala e posição
        adjustModel(model);

        return model;
    }

    private static void adjustModel(Node model) {
        // Escala padrão para modelos OBJ
        Scale scale = new Scale(1.0, 1.0, 1.0);
        model.getTransforms().add(scale);

        // Centralizar
        model.setTranslateX(0);
        model.setTranslateY(0);
        model.setTranslateZ(0);
    }
}

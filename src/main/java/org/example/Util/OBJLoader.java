package org.example.Util;

import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Translate;

public class OBJLoader {

    public static Group loadObj(String path) {
        ObjModelImporter importer = new ObjModelImporter();
        try {
            importer.read(path);
            Group group = new Group();
            MeshView[] meshViews = importer.getImport();

            if (meshViews.length == 0) {
                System.out.println("[OBJLoader] Nenhum MeshView encontrado no arquivo.");
                return group;
            }

            // Adicionar todos os meshes ao grupo
            for (MeshView mesh : meshViews) {
                applyDefaultMaterialIfNeeded(mesh);
                group.getChildren().add(mesh);
            }

            // Calcular o bounding box do grupo completo
            Bounds bounds = group.getBoundsInLocal();

            // Centralizar o grupo na origem (0,0,0)
            centerGroupAtOrigin(group, bounds);

            System.out.println(String.format(
                    "[OBJLoader] Modelo carregado e centralizado. " +
                            "Bounds: [%.2f, %.2f, %.2f] to [%.2f, %.2f, %.2f]",
                    bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(),
                    bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()
            ));

            return group;
        } catch (Exception e) {
            System.err.println("[OBJLoader] Erro ao carregar modelo: " + e.getMessage());
            e.printStackTrace();
            return new Group();
        }
    }

    /**
     * Aplica material padrão se o mesh não tiver material
     */
    private static void applyDefaultMaterialIfNeeded(MeshView mesh) {
        if (mesh.getMaterial() == null) {
            PhongMaterial defaultMaterial = new PhongMaterial();
            defaultMaterial.setDiffuseColor(Color.LIGHTGRAY);
            defaultMaterial.setSpecularColor(Color.WHITE);
            defaultMaterial.setSpecularPower(32.0);
            mesh.setMaterial(defaultMaterial);
            System.out.println("[OBJLoader] Material padrão aplicado.");
        }
    }

    /**
     * Centraliza o grupo na origem (0,0,0) do sistema de coordenadas
     */
    private static void centerGroupAtOrigin(Group group, Bounds bounds) {
        if (bounds.isEmpty()) {
            System.out.println("[OBJLoader] Bounds vazio, não é possível centralizar.");
            return;
        }

        // Calcular o centro atual do grupo
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;
        double centerZ = bounds.getMinZ() + bounds.getDepth() / 2;

        // Calcular a translação necessária para mover o centro para a origem
        double translateX = -centerX;
        double translateY = -centerY;
        double translateZ = -centerZ;

        // Aplicar a translação a todos os filhos do grupo
        for (Node node : group.getChildren()) {
            if (node instanceof MeshView) {
                MeshView mesh = (MeshView) node;
                // Usar transformação de tradução para manter a integridade do mesh
                mesh.getTransforms().add(new Translate(translateX, translateY, translateZ));
            }
        }

        System.out.println(String.format(
                "[OBJLoader] Modelo centralizado. " +
                        "Centro original: (%.2f, %.2f, %.2f), " +
                        "Translação aplicada: (%.2f, %.2f, %.2f)",
                centerX, centerY, centerZ, translateX, translateY, translateZ
        ));
    }


    public static String getModelInfo(Group model) {
        Bounds bounds = model.getBoundsInLocal();
        return String.format(
                "Min: (%.2f, %.2f, %.2f) | Max: (%.2f, %.2f, %.2f) | " +
                        "Center: (%.2f, %.2f, %.2f) | Size: (%.2f, %.2f, %.2f)",
                bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(),
                bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ(),
                bounds.getMinX() + bounds.getWidth() / 2,
                bounds.getMinY() + bounds.getHeight() / 2,
                bounds.getMinZ() + bounds.getDepth() / 2,
                bounds.getWidth(), bounds.getHeight(), bounds.getDepth()
        );
    }
}
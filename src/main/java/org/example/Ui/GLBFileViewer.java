package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

import java.io.File;

public class GLBFileViewer extends SimpleApplication {

    private Spatial model;
    private Node modelNode;
    private Picture botaoMedicao;
    private boolean medindo = false;
    private Geometry ponto1 = null;
    private Geometry ponto2 = null;

    public static void main(String[] args) {
        GLBFileViewer app = new GLBFileViewer();

        // 🚨 Corrige o problema de OpenAL
        AppSettings settings = new AppSettings(true);
        settings.setAudioRenderer(null); // desativa áudio
        app.setSettings(settings);
        app.setShowSettings(false);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Ajustes de câmera
        flyCam.setMoveSpeed(10);
        cam.setLocation(new Vector3f(0, 3, 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        // Luz ambiente
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        // Carregar o modelo GLB
        assetManager.registerLocator("C:/Users/labsfiap/Desktop", FileLocator.class);
        model = assetManager.loadModel("boneco.glb");
        modelNode = new Node("modelNode");
        modelNode.attachChild(model);
        rootNode.attachChild(modelNode);

        // Criar botão de medição
        botaoMedicao = new Picture("BotaoMedicao");
        botaoMedicao.setImage(assetManager, "Interface/Logo/Monkey.png", true); // usa o logo padrão
        botaoMedicao.setWidth(100);
        botaoMedicao.setHeight(40);
        botaoMedicao.setPosition(10, 10);
        guiNode.attachChild(botaoMedicao);

        // Configurar input
        setupInput();
    }

    private void setupInput() {
        // Rotação horizontal
        inputManager.addMapping("RotateLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("RotateRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));

        // Rotação vertical
        inputManager.addMapping("RotateUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("RotateDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));

        // Zoom
        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        // Clique
        inputManager.addMapping("MouseLeftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("BotaoClique", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // Listeners
        inputManager.addListener(analogListener,
                "RotateLeft", "RotateRight", "RotateUp", "RotateDown",
                "ZoomIn", "ZoomOut");
        inputManager.addListener(actionListener, "MouseLeftClick");
        inputManager.addListener(botaoListener, "BotaoClique");
    }

    private final AnalogListener analogListener = (name, value, tpf) -> {
        if (modelNode != null) {
            switch (name) {
                case "RotateRight" -> modelNode.rotate(0, -tpf, 0);
                case "RotateLeft" -> modelNode.rotate(0, tpf, 0);
                case "RotateUp" -> modelNode.rotate(-tpf, 0, 0);
                case "RotateDown" -> modelNode.rotate(tpf, 0, 0);
                case "ZoomIn" -> cam.setLocation(cam.getLocation().add(cam.getDirection().mult(tpf * 5)));
                case "ZoomOut" -> cam.setLocation(cam.getLocation().subtract(cam.getDirection().mult(tpf * 5)));
            }
        }
    };

    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if ("MouseLeftClick".equals(name) && isPressed && medindo) {
            Vector3f click3d = cam.getWorldCoordinates(
                    inputManager.getCursorPosition(), 0f).clone();
            Box box = new Box(0.1f, 0.1f, 0.1f);
            Geometry ponto = new Geometry("Ponto", box);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Red);
            ponto.setMaterial(mat);
            ponto.setLocalTranslation(click3d);
            rootNode.attachChild(ponto);

            if (ponto1 == null) {
                ponto1 = ponto;
            } else if (ponto2 == null) {
                ponto2 = ponto;
                calcularDistancia();
                ponto1 = null;
                ponto2 = null;
            }
        }
    };

    private final ActionListener botaoListener = (name, isPressed, tpf) -> {
        if ("BotaoClique".equals(name) && isPressed) {
            medindo = !medindo;
            if (medindo) {
                System.out.println("Modo medição ativado.");
            } else {
                System.out.println("Modo medição desativado.");
            }
        }
    };

    private void calcularDistancia() {
        if (ponto1 != null && ponto2 != null) {
            float distancia = ponto1.getLocalTranslation().distance(ponto2.getLocalTranslation());
            System.out.println("Distância medida: " + distancia);
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {}

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        super.reshape(vp, w, h);
    }
}

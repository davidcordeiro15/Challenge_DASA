package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;

import java.io.File;

public class GLBFileViewer extends SimpleApplication {

    private static String glbFilePath;
    private Spatial loadedModel;
    private float orbitRadius = 10f;
    private float camYaw = 0f;
    private float camPitch = 0f;
    private final float ROTATION_SPEED = 100f;
    private final float ZOOM_SPEED = 100f;
    private boolean isMousePressed = false;

    public static void setGlbFilePath(String path) {
        glbFilePath = path;
    }

    @Override
    public void simpleInitApp() {
        if (glbFilePath == null || !glbFilePath.toLowerCase().endsWith(".glb")) {
            System.err.println("Arquivo .glb inválido.");
            stop();
            return;
        }

        File file = new File(glbFilePath);
        if (!file.exists()) {
            System.err.println("Arquivo não encontrado: " + glbFilePath);
            stop();
            return;
        }

        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(0, 0, orbitRadius));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        assetManager.registerLocator(file.getParent(), FileLocator.class);

        try {
            loadedModel = assetManager.loadModel(file.getName());
            rootNode.attachChild(loadedModel);
        } catch (Exception e) {
            System.err.println("Erro ao carregar o modelo:");
            e.printStackTrace();
            stop();
        }

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -2, -3).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        viewPort.setBackgroundColor(ColorRGBA.White); // Fundo branco
        addBlueFrame(); // Moldura azul bebê
        setupInput();
    }

    private void addBlueFrame() {
        ColorRGBA lightBlue = new ColorRGBA(0.7f, 0.85f, 1f, 1f);

        float thickness = 20f;
        float width = settings.getWidth();
        float height = settings.getHeight();

        // Top
        createFrame(0, height - thickness, width, thickness, lightBlue);
        // Bottom
        createFrame(0, 0, width, thickness, lightBlue);
        // Left
        createFrame(0, 0, thickness, height, lightBlue);
        // Right
        createFrame(width - thickness, 0, thickness, height, lightBlue);
    }

    private void createFrame(float x, float y, float w, float h, ColorRGBA color) {
        Picture border = new Picture("Border");
        border.setPosition(x, y);
        border.setWidth(w);
        border.setHeight(h);
        guiNode.attachChild(border);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        border.setMaterial(mat);
    }

    private void setupInput() {
        inputManager.addMapping("RotateRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("RotateLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("RotateDown", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("RotateUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("MouseLeftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(analogListener, "RotateRight", "RotateLeft", "RotateUp", "RotateDown", "ZoomIn", "ZoomOut");
        inputManager.addListener(actionListener, "MouseLeftClick");
    }

    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if (name.equals("MouseLeftClick")) {
            isMousePressed = isPressed;
        }
    };

    private final AnalogListener analogListener = (name, value, tpf) -> {
        if (!isMousePressed) return;

        switch(name) {
            case "RotateRight":
                camYaw -= ROTATION_SPEED * tpf * value*4;
                break;
            case "RotateLeft":
                camYaw += ROTATION_SPEED * tpf * value*4;
                break;
            case "RotateDown":
                camPitch -= ROTATION_SPEED * tpf * value*4;
                camPitch = FastMath.clamp(camPitch, -FastMath.HALF_PI + 0.1f, FastMath.HALF_PI - 0.1f);
                break;
            case "RotateUp":
                camPitch += ROTATION_SPEED * tpf * value*4;
                camPitch = FastMath.clamp(camPitch, -FastMath.HALF_PI + 0.1f, FastMath.HALF_PI - 0.1f);
                break;
            case "ZoomIn":
                orbitRadius -= ZOOM_SPEED * tpf * 10;
                orbitRadius = FastMath.clamp(orbitRadius, 1f, 100f);
                break;
            case "ZoomOut":
                orbitRadius += ZOOM_SPEED * tpf * 10;
                orbitRadius = FastMath.clamp(orbitRadius, 1f, 100f);
                break;
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        updateCameraPosition();
    }

    private void updateCameraPosition() {
        float x = orbitRadius * FastMath.sin(camYaw) * FastMath.cos(camPitch);
        float y = orbitRadius * FastMath.sin(camPitch);
        float z = orbitRadius * FastMath.cos(camYaw) * FastMath.cos(camPitch);

        cam.setLocation(new Vector3f(x, y, z));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        // render opcional
    }
}

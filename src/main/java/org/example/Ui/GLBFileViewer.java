package org.example.Ui;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.*;
import com.jme3.scene.shape.Sphere;
import com.jme3.ui.Picture;
import com.jme3.collision.*;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.texture.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.nio.ByteBuffer;

public class GLBFileViewer extends SimpleApplication {

    private static String glbFilePath;
    private Spatial loadedModel;
    private float orbitRadius = 10f;
    private float camYaw = 0f;
    private float camPitch = 0f;
    private final float ROTATION_SPEED = 1f;
    private final float ZOOM_SPEED = 2f;
    private boolean isMousePressed = false;
    private Picture botaoMedicao;
    private float botaoX, botaoY, botaoLargura = 100, botaoAltura = 30;
    private List<Geometry> pontos = new ArrayList<>();
    private boolean modoMedicao = false;
    private final float MIN_ZOOM = 0.5f;
    private final float MAX_ZOOM = 50f;
    private final float ZOOM_SENSITIVITY = 1f;
    private final float ROTATION_SENSITIVITY = 2f;

    public static void setGlbFilePath(String path) {
        glbFilePath = path;
    }

    @Override
    public void simpleInitApp() {
        // Configurações básicas da cena
        orbitRadius = 3f;  // Reduzi a distância inicial
        setupScene();

        // Carrega o modelo GLB
        if (!loadGLBModel()) {
            return;
        }

        // Configurações de iluminação
        setupLighting();

        // Configurações de interface
        setupUI();

        // Configurações de input
        setupInput();
    }

    private void setupScene() {
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(0, 0, orbitRadius));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        viewPort.setBackgroundColor(ColorRGBA.LightGray);
    }

    private boolean loadGLBModel() {
        if (glbFilePath == null || !glbFilePath.toLowerCase().endsWith(".glb")) {
            System.err.println("Arquivo .glb inválido.");
            stop();
            return false;
        }

        File file = new File(glbFilePath);
        if (!file.exists()) {
            System.err.println("Arquivo não encontrado: " + glbFilePath);
            stop();
            return false;
        }

        try {
            assetManager.registerLocator(file.getParent(), FileLocator.class);
            loadedModel = assetManager.loadModel(file.getName());
            loadedModel.setLocalScale(0.1f); // Ajuste de escala padrão
            rootNode.attachChild(loadedModel);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao carregar o modelo:");
            e.printStackTrace();
            stop();
            return false;
        }
    }

    private void setupLighting() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -2, -3).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

    private void setupUI() {
        criarBotaoMedicao();
        addBlueFrame();
    }

    private void criarBotaoMedicao() {
        // Cria uma textura simples para o botão
        Image img = new Image(Image.Format.RGBA8, (int)botaoLargura, (int)botaoAltura,
                ByteBuffer.allocateDirect((int)(botaoLargura * botaoAltura * 4)));
        ImageRaster raster = ImageRaster.create(img);
        for (int y = 0; y < botaoAltura; y++) {
            for (int x = 0; x < botaoLargura; x++) {
                raster.setPixel(x, y, ColorRGBA.Blue);
            }
        }

        Texture2D tex = new Texture2D(img);
        botaoMedicao = new Picture("BotaoMedicao");
        botaoMedicao.setTexture(assetManager, tex, true);
        botaoMedicao.setWidth(botaoLargura);
        botaoMedicao.setHeight(botaoAltura);

        // Posiciona no canto superior direito
        botaoX = settings.getWidth() - botaoLargura - 20;
        botaoY = settings.getHeight() - botaoAltura - 20;

        botaoMedicao.setPosition(botaoX, botaoY);
        guiNode.attachChild(botaoMedicao);
    }

    private void addBlueFrame() {
        ColorRGBA lightBlue = new ColorRGBA(0.7f, 0.85f, 1f, 1f);
        float thickness = 20f;
        float width = settings.getWidth();
        float height = settings.getHeight();

        createFrame(0, height - thickness, width, thickness, lightBlue); // Topo
        createFrame(0, 0, width, thickness, lightBlue); // Base
        createFrame(0, 0, thickness, height, lightBlue); // Esquerda
        createFrame(width - thickness, 0, thickness, height, lightBlue); // Direita
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
        // Mapeamento de controles
        inputManager.addMapping("Rotate", new MouseAxisTrigger(MouseInput.AXIS_X, true),
                new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("Tilt", new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("MouseLeftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(analogListener, "Rotate", "Tilt", "ZoomIn", "ZoomOut");
        inputManager.addListener(actionListener, "MouseLeftClick");


    }

    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if (modoMedicao && name.equals("MouseLeftClick") && isPressed) {
            handlePointCreation();
        }

        if (name.equals("MouseLeftClick")) {
            isMousePressed = isPressed;
        }
    };

    private void handlePointCreation() {
        if (!modoMedicao) return;

        CollisionResults results = new CollisionResults();
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(click2d, 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();

        Ray ray = new Ray(click3d, dir);
        loadedModel.collideWith(ray, results);

        if (results.size() > 0) {
            Vector3f ponto = results.getClosestCollision().getContactPoint();
            adicionarPontoMedicao(ponto);

            if (pontos.size() >= 2) {
                calcularDistancia();
            }
        }
    }

    private final ActionListener botaoListener = (name, isPressed, tpf) -> {
        if (name.equals("BotaoClique") && isPressed) {
            handleButtonClick();
        }
    };

    private void handleButtonClick() {
        Vector2f mouse = inputManager.getCursorPosition();
        float mx = mouse.x;
        float my = settings.getHeight() - mouse.y;

        if (mx >= botaoX && mx <= botaoX + botaoLargura &&
                my >= botaoY && my <= botaoY + botaoAltura) {
            modoMedicao = !modoMedicao;
            atualizarBotaoMedicao();

            if (!modoMedicao && !pontos.isEmpty()) {
                limparPontos();
            }
        }
    }

    private void atualizarBotaoMedicao() {
        Image img = new Image(Image.Format.RGBA8, (int)botaoLargura, (int)botaoAltura,
                ByteBuffer.allocateDirect((int)(botaoLargura * botaoAltura * 4)));
        ImageRaster raster = ImageRaster.create(img);
        ColorRGBA cor = modoMedicao ? ColorRGBA.Green : ColorRGBA.Blue;

        for (int y = 0; y < botaoAltura; y++) {
            for (int x = 0; x < botaoLargura; x++) {
                raster.setPixel(x, y, cor);
            }
        }

        Texture2D tex = new Texture2D(img);
        botaoMedicao.setTexture(assetManager, tex, true);
    }

    private void adicionarPontoMedicao(Vector3f local) {
        Sphere esfera = new Sphere(10, 10, 0.05f);
        Geometry ponto = new Geometry("PontoMedicao", esfera);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        ponto.setMaterial(mat);
        ponto.setLocalTranslation(local);
        rootNode.attachChild(ponto);

        pontos.add(ponto);

        if (pontos.size() > 1) {
            System.out.println("Ponto " + pontos.size() + " adicionado em: " + local);
        }
    }

    private void calcularDistancia() {
        if (pontos.size() < 2) return;

        Geometry ponto1 = pontos.get(pontos.size() - 2);
        Geometry ponto2 = pontos.get(pontos.size() - 1);

        Vector3f pos1 = ponto1.getWorldTranslation();
        Vector3f pos2 = ponto2.getWorldTranslation();

        float distancia = pos1.distance(pos2);
        System.out.println("----------------------------------");
        System.out.printf("Distância entre pontos: %.4f unidades\n", distancia);
        System.out.printf("Ponto 1: %s\n", pos1.toString());
        System.out.printf("Ponto 2: %s\n", pos2.toString());
        System.out.println("----------------------------------");
    }


    private void limparPontos() {
        for (Geometry ponto : pontos) {
            rootNode.detachChild(ponto);
        }
        pontos.clear();
        System.out.println("Todos os pontos de medição foram removidos.");
    }

    private final AnalogListener analogListener = (name, value, tpf) -> {
        switch(name) {
            case "RotateRight":
                camYaw -= ROTATION_SPEED * value;
                break;
            case "RotateLeft":
                camYaw += ROTATION_SPEED * value;
                break;
            case "RotateDown":
                camPitch -= ROTATION_SPEED * value;
                camPitch = FastMath.clamp(camPitch, -FastMath.HALF_PI + 0.1f, FastMath.HALF_PI - 0.1f);
                break;
            case "RotateUp":
                camPitch += ROTATION_SPEED * value;
                camPitch = FastMath.clamp(camPitch, -FastMath.HALF_PI + 0.1f, FastMath.HALF_PI - 0.1f);
                break;
            case "ZoomIn":
                orbitRadius -= ZOOM_SENSITIVITY * value;
                orbitRadius = FastMath.clamp(orbitRadius, MIN_ZOOM, MAX_ZOOM);
                break;
            case "ZoomOut":
                orbitRadius += ZOOM_SENSITIVITY * value;
                orbitRadius = FastMath.clamp(orbitRadius, MIN_ZOOM, MAX_ZOOM);
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
        // Renderização customizada pode ser adicionada aqui
    }
}
package org.example.Ui;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.Util.OBJLoader;
import javafx.scene.input.PickResult;
import java.util.ArrayList;
import java.util.List;

public class GLBFileViewer extends Application {

    private static String glbFilePath;
    private Group sceneRoot;
    private PerspectiveCamera camera;
    private double camDistance = 5.0;
    private double camYaw = 0;
    private double camPitch = 20;
    private final double MIN_PITCH = -89, MAX_PITCH = 89;
    private double lastX, lastY;
    private double panX = 0, panY = 0;

    private List<Sphere> spheres = new ArrayList<>();
    private RotateTransition currentRotation;
    private Label infoLabel;
    private Node loadedModel;

    public static void setGlbFilePath(String path) {
        glbFilePath = path;
    }

    @Override
    public void start(Stage primaryStage) {
        if (glbFilePath == null) {
            System.out.println("Nenhum arquivo GLB selecionado.");
            return;
        }

        // Configurar cena principal
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1200, 800, true);
        scene.setFill(Color.web("#2b2b2b"));

        // Área de visualização 3D
        StackPane viewport = new StackPane();
        viewport.setStyle("-fx-background-color: #2b2b2b;");

        // Label de informações
        infoLabel = new Label("Carregando modelo...");
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");
        root.setBottom(infoLabel);

        // Configurar cena 3D
        sceneRoot = new Group();
        setupCamera();
        setupLights();

        SubScene subScene = new SubScene(sceneRoot, 1200, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#2b2b2b"));
        subScene.setCamera(camera);

        viewport.getChildren().add(subScene);
        root.setCenter(viewport);

        // Configurar controles de mouse
        setupMouseControls(subScene);

        // Carregar o modelo GLB
        loadGLBModel();

        primaryStage.setTitle("Visualizador GLB 3D - " + glbFilePath);
        primaryStage.setScene(scene);
        primaryStage.show();

        infoLabel.setText("Modelo carregado! Use:\n- Botão esquerdo: Rotacionar\n- SHIFT + esquerdo: Pan\n- Scroll: Zoom\n- Botão direito: Zoom rápido\n- Duplo clique: Rotacionar objeto");
    }

    private void loadGLBModel() {
        try {
            loadedModel = OBJLoader.loadOBJ(glbFilePath);
            sceneRoot.getChildren().add(loadedModel);
            infoLabel.setText("Modelo carregado: " + glbFilePath);
        } catch (Exception e) {
            infoLabel.setText("Erro ao carregar modelo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCamera() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(60);
        updateCameraPosition();
    }

    private void setupLights() {
        // Luz ambiente
        AmbientLight ambient = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        sceneRoot.getChildren().add(ambient);

        // Luz principal
        PointLight mainLight = new PointLight(Color.WHITE);
        mainLight.setTranslateX(10);
        mainLight.setTranslateY(-15);
        mainLight.setTranslateZ(-10);
        sceneRoot.getChildren().add(mainLight);

        // Luz de preenchimento
        PointLight fillLight = new PointLight(Color.color(0.3, 0.3, 0.5));
        fillLight.setTranslateX(-10);
        fillLight.setTranslateY(-5);
        fillLight.setTranslateZ(5);
        sceneRoot.getChildren().add(fillLight);
    }

    private void setupMouseControls(SubScene subScene) {
        subScene.setOnMousePressed(this::onMousePressed);
        subScene.setOnMouseDragged(this::onMouseDragged);
        subScene.setOnScroll(this::onScroll);
        subScene.setOnMouseClicked(this::onMouseClicked);
    }

    private void onMousePressed(MouseEvent e) {
        lastX = e.getSceneX();
        lastY = e.getSceneY();
    }

    private void onMouseDragged(MouseEvent e) {
        double dx = e.getSceneX() - lastX;
        double dy = e.getSceneY() - lastY;
        lastX = e.getSceneX();
        lastY = e.getSceneY();

        // Rotação
        if (e.isPrimaryButtonDown() && !e.isShiftDown()) {
            camYaw = (camYaw - dx * 0.3) % 360;
            camPitch = clamp(camPitch - dy * 0.3, MIN_PITCH, MAX_PITCH);
            updateCameraPosition();
        }
        // Pan
        else if ((e.isPrimaryButtonDown() && e.isShiftDown()) || e.isMiddleButtonDown()) {
            panX += -dx * 0.005 * camDistance;
            panY += dy * 0.005 * camDistance;
            updateCameraPosition();
        }
        // Zoom com botão direito
        else if (e.isSecondaryButtonDown()) {
            camDistance = Math.max(0.2, camDistance + dy * 0.01);
            updateCameraPosition();
        }
    }

    private void onScroll(javafx.scene.input.ScrollEvent e) {
        double delta = e.getDeltaY();
        camDistance = Math.max(0.2, camDistance - delta * 0.01);
        updateCameraPosition();
    }

    private void onMouseClicked(MouseEvent e) {
        // Marcar ponto com botão direito + SHIFT
        if (e.isSecondaryButtonDown() && e.isShiftDown()) {
            PickResult pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode() != null) {
                Point3D worldPoint = pickResult.getIntersectedPoint();
                addSphereAt(worldPoint);
            }
        }
        // Rotacionar objeto com duplo clique
        else if (e.getClickCount() == 2 && e.getButton().toString().equals("PRIMARY")) {
            PickResult pickResult = e.getPickResult();
            Node pickedNode = pickResult.getIntersectedNode();
            if (pickedNode != null && pickedNode != sceneRoot) {
                rotateObject(pickedNode);
            }
        }
    }

    private void updateCameraPosition() {
        double yawRad = Math.toRadians(camYaw);
        double pitchRad = Math.toRadians(camPitch);

        double x = camDistance * Math.cos(pitchRad) * Math.sin(yawRad);
        double y = camDistance * Math.sin(pitchRad);
        double z = camDistance * Math.cos(pitchRad) * Math.cos(yawRad);

        camera.setTranslateX(x + panX);
        camera.setTranslateY(y + panY);
        camera.setTranslateZ(z);

        camera.getTransforms().clear();
        camera.getTransforms().add(new Rotate(-camPitch, Rotate.X_AXIS));
        camera.getTransforms().add(new Rotate(-camYaw, Rotate.Y_AXIS));
    }

    private void addSphereAt(Point3D point) {
        Sphere sphere = new Sphere(0.05);
        PhongMaterial material = new PhongMaterial(Color.RED);
        sphere.setMaterial(material);
        sphere.setTranslateX(point.getX());
        sphere.setTranslateY(point.getY());
        sphere.setTranslateZ(point.getZ());

        sceneRoot.getChildren().add(sphere);
        spheres.add(sphere);

        updateDistanceInfo();
    }

    private void updateDistanceInfo() {
        if (spheres.size() >= 2) {
            Sphere a = spheres.get(spheres.size() - 2);
            Sphere b = spheres.get(spheres.size() - 1);
            double dist = calculateDistance(a, b);
            infoLabel.setText(String.format("Distância: %.4f unidades | Total de pontos: %d",
                    dist, spheres.size()));
        } else {
            infoLabel.setText(String.format("Pontos colocados: %d", spheres.size()));
        }
    }

    private double calculateDistance(Sphere a, Sphere b) {
        return Math.sqrt(
                Math.pow(a.getTranslateX() - b.getTranslateX(), 2) +
                        Math.pow(a.getTranslateY() - b.getTranslateY(), 2) +
                        Math.pow(a.getTranslateZ() - b.getTranslateZ(), 2)
        );
    }

    private void rotateObject(Node node) {
        if (currentRotation != null) {
            currentRotation.stop();
        }

        currentRotation = new RotateTransition(Duration.seconds(3), node);
        currentRotation.setAxis(Rotate.Y_AXIS);
        currentRotation.setByAngle(360);
        currentRotation.setCycleCount(Animation.INDEFINITE);
        currentRotation.play();

        infoLabel.setText("Rotacionando objeto...");
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Método para iniciar a aplicação
    public void start() {
        // Iniciar em uma nova thread para não conflitar com Swing
        new Thread(() -> {
            try {
                Application.launch(GLBFileViewer.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        // Primeiro abrir o seletor de arquivos
        FileSelector selector = new FileSelector();
        String filePath = selector.showDisplay();

        if (filePath != null) {
            setGlbFilePath(filePath);
            launch(args);
        }
    }
}
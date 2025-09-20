package org.example.Controller;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.Util.OBJLoader;
import javafx.scene.input.PickResult;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller integrado para visualização 3D dentro do FXML Tela3D.fxml
 * Funcionalidades:
 * - Carregamento de modelos OBJ
 * - Controles de câmera intuitivos (mouse invertido)
 * - Sistema de medição com pontos
 * - Controles WASD para movimento
 */
public class ViewerController implements Initializable {

    @FXML private StackPane canvasContainer;
    @FXML private ToggleButton togglePlace;
    @FXML private Label lblInfo;

    // Componentes da cena 3D
    private Group sceneRoot;
    private PerspectiveCamera camera;
    private SubScene subScene;
    private Node loadedModel;
    private Point3D modelCenter = new Point3D(0, 0, 0);
    private double modelRadius = 1.0;

    // Configurações da câmera (sistema orbital - velocidades otimizadas)
    private Point3D cameraTarget = new Point3D(0, 0, 0); // Ponto que a câmera olha
    private double cameraDistance = 5.0;    // Distância do alvo
    private double cameraYaw = 0;           // Rotação horizontal (azimuth)
    private double cameraPitch = 20;        // Rotação vertical (elevation)
    private final double MIN_PITCH = -89, MAX_PITCH = 89;
    private final double MIN_DISTANCE = 0.1;

    // 🎯 VELOCIDADES REDUZIDAS (mais suaves)
    private final double CAMERA_SPEED = 0.15;        // Velocidade reduzida em 50%
    private final double CAMERA_SPEED_FAST = 0.4;    // Velocidade rápida reduzida
    private final double MOUSE_SENSITIVITY = 0.8;    // Sensibilidade reduzida
    private final double ZOOM_SPEED = 0.08;          // Zoom mais suave

    // ⚡ CONFIGURAÇÕES DE ACELERAÇÃO
    private double currentSpeedMultiplier = 1.0;         // Multiplicador dinâmico de velocidade
    private final double ACCELERATION_RATE = 0.03;       // Taxa de aceleração reduzida
    private final double MAX_ACCELERATION = 2.0;         // Máxima aceleração reduzida

    // Controles de entrada
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private double lastMouseX, lastMouseY;
    private boolean mousePressed = false;

    // Sistema de medição
    private List<Sphere> spheres = new ArrayList<>();
    private RotateTransition currentRotation;
    private boolean placeModeEnabled = false;

    // Timer para movimento suave
    private AnimationTimer movementTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupScene3D();
        updateInfoLabel();
    }

    /**
     * Carrega um modelo automaticamente na inicialização (chamado pelo LoginController)
     */
    public void loadModelOnStartup(String objPath) {
        if (objPath != null && !objPath.isEmpty()) {
            javafx.application.Platform.runLater(() -> {
                loadOBJModel(objPath);
            });
        }
    }

    private void setupScene3D() {
        sceneRoot = new Group();
        setupCamera();
        setupLights();

        subScene = new SubScene(sceneRoot, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#2b2b2b"));
        subScene.setCamera(camera);
        subScene.setFocusTraversable(true);

        canvasContainer.getChildren().add(subScene);
        subScene.widthProperty().bind(canvasContainer.widthProperty());
        subScene.heightProperty().bind(canvasContainer.heightProperty());

        setupMouseControls();
        setupKeyboardControls();
        startMovementTimer();
    }

    private void setupCamera() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(60);
        updateCameraTransform();
    }

    private void setupLights() {
        sceneRoot.getChildren().removeIf(node ->
                node instanceof AmbientLight ||
                        node instanceof PointLight ||
                        node instanceof DirectionalLight
        );

        AmbientLight ambient = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        sceneRoot.getChildren().add(ambient);

        PointLight mainLight = new PointLight(Color.WHITE);
        mainLight.setTranslateX(modelCenter.getX() + modelRadius);
        mainLight.setTranslateY(modelCenter.getY() - modelRadius);
        mainLight.setTranslateZ(modelCenter.getZ() + modelRadius);
        sceneRoot.getChildren().add(mainLight);

        PointLight fillLight = new PointLight(Color.color(0.3, 0.3, 0.5));
        fillLight.setTranslateX(modelCenter.getX() - modelRadius);
        fillLight.setTranslateY(modelCenter.getY() + modelRadius/2);
        fillLight.setTranslateZ(modelCenter.getZ() - modelRadius);
        sceneRoot.getChildren().add(fillLight);
    }

    private void setupMouseControls() {
        subScene.setOnMousePressed(this::onMousePressed);
        subScene.setOnMouseDragged(this::onMouseDragged);
        subScene.setOnMouseReleased(this::onMouseReleased);
        subScene.setOnScroll(this::onScroll);
        subScene.setOnMouseClicked(this::onMouseClicked);
    }

    private void setupKeyboardControls() {
        canvasContainer.setOnKeyPressed(this::onKeyPressed);
        canvasContainer.setOnKeyReleased(this::onKeyReleased);
        canvasContainer.setFocusTraversable(true);
        subScene.setOnKeyPressed(this::onKeyPressed);
        subScene.setOnKeyReleased(this::onKeyReleased);
    }

    private void startMovementTimer() {
        if (movementTimer != null) return;

        movementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleMovement();
            }
        };
        movementTimer.start();
    }

    private void handleMovement() {
        try {
            if (pressedKeys.isEmpty()) return;

            double yawRad = Math.toRadians(cameraYaw);
            double pitchRad = Math.toRadians(cameraPitch);

            Point3D forward = new Point3D(Math.sin(yawRad), 0, -Math.cos(yawRad));
            Point3D right = new Point3D(Math.cos(yawRad), 0, Math.sin(yawRad));
            Point3D up = new Point3D(0, 1, 0);

            Point3D movement = new Point3D(0, 0, 0);
            double speed = CAMERA_SPEED * (cameraDistance / 5.0);

            if (pressedKeys.contains(KeyCode.W)) movement = movement.add(forward.multiply(speed));
            if (pressedKeys.contains(KeyCode.S)) movement = movement.add(forward.multiply(-speed));
            if (pressedKeys.contains(KeyCode.A)) movement = movement.add(right.multiply(-speed));
            if (pressedKeys.contains(KeyCode.D)) movement = movement.add(right.multiply(speed));
            if (pressedKeys.contains(KeyCode.Q)) movement = movement.add(up.multiply(-speed));
            if (pressedKeys.contains(KeyCode.E)) movement = movement.add(up.multiply(speed));

            if (movement.magnitude() > 0) {
                cameraTarget = cameraTarget.add(movement);
                updateCameraTransform();
            }
        } catch (Exception e) {
            System.out.println("Erro no movimento: " + e.getMessage());
        }
    }

    private void onMousePressed(MouseEvent e) {
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
        mousePressed = true;

        if (e.isMiddleButtonDown()) {
            PickResult pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode() != null && pickResult.getIntersectedNode() != sceneRoot) {
                Point3D worldPoint = pickResult.getIntersectedPoint();
                addSphereAt(worldPoint);
            }
        }

        subScene.requestFocus();
        canvasContainer.requestFocus();
    }

    private void onMouseDragged(MouseEvent e) {
        if (!mousePressed) return;

        double deltaX = e.getSceneX() - lastMouseX;
        double deltaY = e.getSceneY() - lastMouseY;
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();

        if (e.isPrimaryButtonDown() && !e.isShiftDown()) {
            cameraYaw = (cameraYaw - deltaX * MOUSE_SENSITIVITY) % 360;
            cameraPitch = clamp(cameraPitch - deltaY * MOUSE_SENSITIVITY, MIN_PITCH, MAX_PITCH);
            updateCameraTransform();
        } else if (e.isPrimaryButtonDown() && e.isShiftDown()) {
            double yawRad = Math.toRadians(cameraYaw);
            Point3D right = new Point3D(Math.cos(yawRad), 0, Math.sin(yawRad));
            Point3D up = new Point3D(0, 1, 0);

            double panSpeed = 0.008 * cameraDistance; // Pan mais suave
            Point3D panMovement = right.multiply(-deltaX * panSpeed).add(up.multiply(deltaY * panSpeed));
            cameraTarget = cameraTarget.add(panMovement);
            updateCameraTransform();
        } else if (e.isSecondaryButtonDown()) {
            cameraDistance = Math.max(MIN_DISTANCE, cameraDistance + deltaY * 0.008); // Zoom mais suave
            updateCameraTransform();
        }
    }

    private void onMouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    private void onScroll(javafx.scene.input.ScrollEvent e) {
        double delta = e.getDeltaY();
        double zoomFactor = delta > 0 ? 0.9 : 1.1; // Zoom mais suave
        cameraDistance = Math.max(MIN_DISTANCE, cameraDistance * zoomFactor);
        updateCameraTransform();
    }

    private void onMouseClicked(MouseEvent e) {
        // 🎯 Apenas clique do mouse para adicionar pontos (não duplo clique)
        if ((placeModeEnabled && e.isPrimaryButtonDown() && e.getClickCount() == 1) ||
                (e.isMiddleButtonDown() && e.getClickCount() == 1)) {

            PickResult pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode() != null && pickResult.getIntersectedNode() != sceneRoot) {
                Point3D worldPoint = pickResult.getIntersectedPoint();
                addSphereAt(worldPoint);
            }
        }
    }

    private void onKeyPressed(KeyEvent e) {
        pressedKeys.add(e.getCode());
        if (e.getCode() == KeyCode.R) resetCamera();
        else if (e.getCode() == KeyCode.C) onClearSpheres();
    }

    private void onKeyReleased(KeyEvent e) {
        pressedKeys.remove(e.getCode());
    }

    @FXML
    private void onOpenModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar arquivo OBJ");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos OBJ", "*.obj"));
        Stage stage = (Stage) canvasContainer.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) loadOBJModel(selectedFile.getAbsolutePath());
    }

    @FXML
    private void onTogglePlace() {
        placeModeEnabled = togglePlace.isSelected();
        updateInfoLabel();
    }

    @FXML
    private void onAddSphere() {
        Point3D cameraPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D direction = cameraTarget.subtract(cameraPos).normalize();
        double distance = cameraDistance * 0.3;
        Point3D spherePos = cameraPos.add(direction.multiply(distance));
        addSphereAt(spherePos);
    }

    @FXML
    private void onClearSpheres() {
        for (Sphere sphere : spheres) sceneRoot.getChildren().remove(sphere);
        spheres.clear();
        updateInfoLabel();
    }

    private void loadOBJModel(String filePath) {
        try {
            // Remover modelo anterior
            if (loadedModel != null) {
                sceneRoot.getChildren().remove(loadedModel);
            }

            // Carregar novo modelo (já centralizado automaticamente)
            loadedModel = OBJLoader.loadObj(filePath);
            sceneRoot.getChildren().add(loadedModel);

            // Calcular bounds para configuração da câmera
            calculateModelBounds();
            resetCamera();
            setupLights();

            lblInfo.setText("Modelo carregado e centralizado: " + new File(filePath).getName());

            // Debug opcional
            System.out.println(OBJLoader.getModelInfo((Group) loadedModel));

        } catch (Exception e) {
            lblInfo.setText("Erro ao carregar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void calculateModelBounds() {
        if (loadedModel != null) {
            Bounds bounds = loadedModel.getBoundsInLocal();

            // Como o modelo está centralizado, o centro é (0,0,0)
            modelCenter = new Point3D(0, 0, 0);

            modelRadius = Math.max(
                    Math.max(bounds.getWidth(), bounds.getHeight()),
                    bounds.getDepth()
            ) / 2.0;

            if (modelRadius < 0.1) {
                modelRadius = 1.0;
            }
        }
    }

    private void resetCamera() {
        cameraTarget = modelCenter;
        cameraDistance = modelRadius * 3.0;
        cameraYaw = 45;
        cameraPitch = 20;
        updateCameraTransform();
    }

    private void updateCameraTransform() {
        double yawRad = Math.toRadians(cameraYaw);
        double pitchRad = Math.toRadians(cameraPitch);

        double x = cameraTarget.getX() + cameraDistance * Math.cos(pitchRad) * Math.sin(yawRad);
        double y = cameraTarget.getY() + cameraDistance * Math.sin(pitchRad);
        double z = cameraTarget.getZ() + cameraDistance * Math.cos(pitchRad) * Math.cos(yawRad);

        camera.setTranslateX(x);
        camera.setTranslateY(y);
        camera.setTranslateZ(z);

        camera.getTransforms().clear();
        Point3D lookDirection = cameraTarget.subtract(x, y, z).normalize();
        double targetYaw = Math.toDegrees(Math.atan2(lookDirection.getX(), lookDirection.getZ()));
        double targetPitch = Math.toDegrees(Math.asin(-lookDirection.getY()));
        camera.getTransforms().add(new Rotate(-targetPitch, Rotate.X_AXIS));
        camera.getTransforms().add(new Rotate(-targetYaw, Rotate.Y_AXIS));
    }

    private void addSphereAt(Point3D point) {
        double sphereSize = Math.max(modelRadius * 0.015, 0.02);
        Sphere sphere = new Sphere(sphereSize);
        PhongMaterial material = new PhongMaterial(Color.RED);
        sphere.setMaterial(material);
        sphere.setTranslateX(point.getX());
        sphere.setTranslateY(point.getY());
        sphere.setTranslateZ(point.getZ());

        sceneRoot.getChildren().add(sphere);
        spheres.add(sphere);
        javafx.application.Platform.runLater(this::updateInfoLabel);
    }

    private void rotateObject(Node node) {
        if (currentRotation != null) currentRotation.stop();
        currentRotation = new RotateTransition(Duration.seconds(3), node);
        currentRotation.setAxis(Rotate.Y_AXIS);
        currentRotation.setByAngle(360);
        currentRotation.setCycleCount(Animation.INDEFINITE);
        currentRotation.play();
    }

    private void updateInfoLabel() {
        StringBuilder info = new StringBuilder();

        if (spheres.size() == 0) {
            info.append("Nenhum ponto marcado");
        } else if (spheres.size() == 1) {
            info.append("1 ponto marcado");
        } else {
            // Calcular distância apenas entre as bolinhas geradas pelo mouse
            Sphere a = spheres.get(spheres.size() - 2);
            Sphere b = spheres.get(spheres.size() - 1);
            double distance = calculateDistance(a, b);
            info.append(String.format("Distância entre últimos pontos: %.4f unidades", distance));
        }

        info.append(" | Total: ").append(spheres.size()).append(" pontos");

        if (placeModeEnabled) {
            info.append(" | 🎯 MODO COLOCAR: Clique para adicionar pontos");
        } else {
            info.append(" | 🖱️ MODO NORMAL: Use botões para navegar");
        }

        info.append(" | 🔘 Botão do scroll: Adicionar ponto rápido");

        lblInfo.setText(info.toString());
    }

    private double calculateDistance(Sphere a, Sphere b) {
        return Math.sqrt(
                Math.pow(a.getTranslateX() - b.getTranslateX(), 2) +
                        Math.pow(a.getTranslateY() - b.getTranslateY(), 2) +
                        Math.pow(a.getTranslateZ() - b.getTranslateZ(), 2)
        );
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
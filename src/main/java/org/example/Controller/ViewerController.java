package org.example.Controller;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.example.Util.OBJLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewerController {

    @FXML private StackPane canvasContainer;
    @FXML private ToggleButton togglePlace;
    @FXML private Label lblInfo;

    // 3D scene
    private SubScene subScene;
    private Group sceneRoot3D;
    private PerspectiveCamera camera;
    private final double initialCamDistance = 5.0;
    private double camDistance = initialCamDistance;
    private double camYaw = 0;
    private double camPitch = 20;
    private final double MIN_PITCH = -89, MAX_PITCH = 89;

    // mouse control state
    private double lastX, lastY;
    private boolean dragging = false;

    // placed spheres
    private final List<Sphere> spheres = new ArrayList<>();

    // model node
    private Node modelNode = null;

    // for panning
    private double panX = 0, panY = 0;

    // current rotation animation
    private RotateTransition currentRotation;

    @FXML
    public void initialize() {
        // build 3D scene
        sceneRoot3D = new Group();
        buildCamera();
        buildLights();

        subScene = new SubScene(sceneRoot3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#2b2b2b"));
        subScene.setCamera(camera);

        // keep subscene resized to container
        canvasContainer.widthProperty().addListener((obs, oldV, newV) -> subScene.setWidth(newV.doubleValue()));
        canvasContainer.heightProperty().addListener((obs, oldV, newV) -> subScene.setHeight(newV.doubleValue()));

        canvasContainer.getChildren().add(subScene);

        // mouse handlers
        setupMouseControls();

        updateCameraPosition();
        lblInfo.setText("Pronto para carregar modelos GLB. Use:\n- Botão esquerdo: Rotacionar\n- SHIFT + esquerdo: Pan\n- Scroll: Zoom\n- Botão direito: Zoom rápido");
    }

    private void setupMouseControls() {
        subScene.setOnMousePressed(this::onMousePressed);
        subScene.setOnMouseDragged(this::onMouseDragged);
        subScene.setOnScroll(this::onScroll);
        subScene.setOnMouseClicked(this::onMouseClicked);
        subScene.setOnMouseMoved(this::onMouseMoved);
    }

    // ---------- UI actions ----------
    @FXML
    private void onOpenModel(ActionEvent ev) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("GLB Files", "*.glb"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("GLTF Files", "*.gltf"));
        Window w = canvasContainer.getScene().getWindow();
        File f = fc.showOpenDialog(w);
        if (f != null) {
            loadModelGLB(f);
        }
    }

    @FXML
    private void onTogglePlace(ActionEvent ev) {
        boolean place = togglePlace.isSelected();
        lblInfo.setText(place ? "Modo marcação ativo. Clique para adicionar pontos." : "Modo marcação desativado.");
    }

    @FXML
    private void onAddSphere(ActionEvent ev) {
        addSphereInFrontOfCamera();
    }

    @FXML
    private void onClearSpheres(ActionEvent ev) {
        for (Sphere s : spheres) sceneRoot3D.getChildren().remove(s);
        spheres.clear();
        lblInfo.setText("Pontos removidos.");
    }

    @FXML
    private void onRotateModel(ActionEvent ev) {
        if (modelNode != null) {
            rotateObject(modelNode);
            lblInfo.setText("Modelo em rotação automática");
        }
    }

    @FXML
    private void onStopRotation(ActionEvent ev) {
        if (currentRotation != null) {
            currentRotation.stop();
            lblInfo.setText("Rotação parada");
        }
    }

    // ---------- model loading ----------
    private void loadModelGLB(File file) {
        // remove previous model
        if (modelNode != null) {
            sceneRoot3D.getChildren().remove(modelNode);
            modelNode = null;
        }

        // stop any current rotation
        if (currentRotation != null) {
            currentRotation.stop();
            currentRotation = null;
        }

        try {
            modelNode = OBJLoader.loadOBJ(String.valueOf(file));
            sceneRoot3D.getChildren().add(modelNode);

            // Ajustar câmera para o novo modelo
            resetCamera();
            lblInfo.setText("Modelo carregado: " + file.getName() + "\nUse duplo clique em qualquer objeto para rotacioná-lo");

        } catch (Exception ex) {
            lblInfo.setText("Erro ao carregar modelo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void resetCamera() {
        camDistance = initialCamDistance;
        camYaw = 0;
        camPitch = 20;
        panX = 0;
        panY = 0;
        updateCameraPosition();
    }

    // ---------- camera and controls ----------
    private void buildCamera() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(60);
    }

    private void buildLights() {
        // Luz ambiente
        AmbientLight ambient = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        sceneRoot3D.getChildren().add(ambient);

        // Luz direcional principal
        PointLight mainLight = new PointLight(Color.WHITE);
        mainLight.setTranslateX(10);
        mainLight.setTranslateY(-15);
        mainLight.setTranslateZ(-10);
        sceneRoot3D.getChildren().add(mainLight);

        // Luz de preenchimento
        PointLight fillLight = new PointLight(Color.color(0.3, 0.3, 0.5));
        fillLight.setTranslateX(-10);
        fillLight.setTranslateY(-5);
        fillLight.setTranslateZ(5);
        sceneRoot3D.getChildren().add(fillLight);
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

        // Orientar a câmera para o ponto de interesse
        camera.getTransforms().clear();
        camera.getTransforms().add(new Rotate(-camPitch, Rotate.X_AXIS));
        camera.getTransforms().add(new Rotate(-camYaw, Rotate.Y_AXIS));
    }

    // ---------- mouse handlers ----------
    private void onMousePressed(MouseEvent e) {
        lastX = e.getSceneX();
        lastY = e.getSceneY();
        dragging = true;
    }

    private void onMouseDragged(MouseEvent e) {
        if (!dragging) return;

        double dx = e.getSceneX() - lastX;
        double dy = e.getSceneY() - lastY;
        lastX = e.getSceneX();
        lastY = e.getSceneY();

        // Rotação (botão esquerdo)
        if (e.isPrimaryButtonDown() && !e.isShiftDown()) {
            camYaw = (camYaw - dx * 0.3) % 360;
            camPitch = clamp(camPitch - dy * 0.3, MIN_PITCH, MAX_PITCH);
            updateCameraPosition();
        }
        // Pan (SHIFT + botão esquerdo ou botão do meio)
        else if ((e.isPrimaryButtonDown() && e.isShiftDown()) || e.isMiddleButtonDown()) {
            panX += -dx * 0.005 * camDistance;
            panY += dy * 0.005 * camDistance;
            updateCameraPosition();
        }
        // Zoom (botão direito)
        else if (e.isSecondaryButtonDown()) {
            camDistance = Math.max(0.2, camDistance + dy * 0.01);
            updateCameraPosition();
        }
    }

    private void onScroll(ScrollEvent e) {
        double delta = e.getDeltaY();
        camDistance = Math.max(0.2, camDistance - delta * 0.01);
        updateCameraPosition();
    }

    private void onMouseClicked(MouseEvent e) {
        if (togglePlace.isSelected() && e.getButton() == MouseButton.PRIMARY) {
            // Tentar pegar ponto de interseção com o modelo
            PickResult pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode() != null) {
                Point3D worldPoint = pickResult.getIntersectedPoint();
                addSphereAt(worldPoint);
            }
        }
        // Duplo clique para rotacionar objeto específico
        else if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
            PickResult pickResult = e.getPickResult();
            Node pickedNode = pickResult.getIntersectedNode();
            if (pickedNode != null && pickedNode != sceneRoot3D) {
                rotateObject(pickedNode);
                lblInfo.setText("Rotacionando objeto: " + pickedNode.getClass().getSimpleName());
            }
        }
    }

    private void onMouseMoved(MouseEvent e) {
        // Atualizar cursor baseado no modo
        if (e.isShiftDown()) {
            subScene.setCursor(Cursor.MOVE);
        } else {
            subScene.setCursor(Cursor.DEFAULT);
        }
    }

    // ---------- sphere placement ----------
    private void addSphereAt(Point3D p) {
        Sphere sphere = new Sphere(0.05);
        PhongMaterial material = new PhongMaterial(Color.RED);
        sphere.setMaterial(material);
        sphere.setTranslateX(p.getX());
        sphere.setTranslateY(p.getY());
        sphere.setTranslateZ(p.getZ());

        sceneRoot3D.getChildren().add(sphere);
        spheres.add(sphere);

        updateDistanceInfo();
    }

    private void addSphereInFrontOfCamera() {
        Point3D camDir = getCameraDirection();
        Point3D camPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D pos = camPos.add(camDir.multiply(2));
        addSphereAt(pos);
    }

    private void updateDistanceInfo() {
        if (spheres.size() >= 2) {
            Sphere a = spheres.get(spheres.size() - 2);
            Sphere b = spheres.get(spheres.size() - 1);
            double dist = calculateDistance(a, b);
            lblInfo.setText(String.format("Distância entre pontos: %.4f unidades", dist));
        } else if (spheres.size() == 1) {
            lblInfo.setText("Primeiro ponto colocado. Clique para adicionar outro ponto.");
        }
    }

    private double calculateDistance(Sphere a, Sphere b) {
        return Math.sqrt(
                Math.pow(a.getTranslateX() - b.getTranslateX(), 2) +
                        Math.pow(a.getTranslateY() - b.getTranslateY(), 2) +
                        Math.pow(a.getTranslateZ() - b.getTranslateZ(), 2)
        );
    }

    // ---------- rotation animation ----------
    private void rotateObject(Node node) {
        // Parar rotação anterior
        if (currentRotation != null) {
            currentRotation.stop();
        }

        currentRotation = new RotateTransition(Duration.seconds(4), node);
        currentRotation.setAxis(Rotate.Y_AXIS);
        currentRotation.setByAngle(360);
        currentRotation.setCycleCount(Animation.INDEFINITE);
        currentRotation.setInterpolator(javafx.animation.Interpolator.LINEAR);
        currentRotation.play();
    }

    // ---------- helper methods ----------
    private Point3D getCameraDirection() {
        Point3D camPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D target = new Point3D(panX, panY, 0);
        return target.subtract(camPos).normalize();
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
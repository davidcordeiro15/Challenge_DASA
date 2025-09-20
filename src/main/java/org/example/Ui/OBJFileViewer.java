package org.example.Ui;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.Util.OBJLoader;
import javafx.scene.input.PickResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Visualizador 3D avançado para arquivos OBJ com controles estilo Unreal Engine
 * Funcionalidades:
 * - Posicionamento automático da câmera baseado no bounding box do modelo
 * - Controles WASD para movimento da câmera
 * - Sistema de medição com marcação de pontos
 * - Rotação automática de objetos
 */
public class OBJFileViewer extends Application {

    // Configurações do arquivo
    private static String objFilePath;

    // Componentes da cena
    private Group sceneRoot;
    private PerspectiveCamera camera;
    private Label infoLabel;
    private Node loadedModel;
    private Point3D modelCenter = new Point3D(0, 0, 0);
    private double modelRadius = 1.0;

    // Configurações da câmera
    private Point3D cameraPosition = new Point3D(0, 0, 5);
    private double cameraYaw = 0;    // Rotação horizontal
    private double cameraPitch = 0;  // Rotação vertical
    private final double MIN_PITCH = -89, MAX_PITCH = 89;
    private final double CAMERA_SPEED = 0.05;      // Velocidade de movimento
    private final double MOUSE_SENSITIVITY = 0.3;  // Sensibilidade do mouse
    private final double ZOOM_SPEED = 0.01;        // Velocidade do zoom

    // Controles de entrada
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private double lastMouseX, lastMouseY;
    private boolean mousePressed = false;

    // Sistema de medição
    private List<Sphere> spheres = new ArrayList<>();
    private RotateTransition currentRotation;

    // Timer para movimento suave
    private AnimationTimer movementTimer;

    /**
     * Define o caminho do arquivo OBJ a ser carregado
     */
    public static void setObjFilePath(String path) {
        objFilePath = path;
    }

    /**
     * Método principal para abrir o visualizador com um arquivo específico
     * Melhorado para evitar fechamento automático
     */
    public static void abrirComArquivo(String path) {
        if (path == null || path.isEmpty()) {
            System.out.println("Nenhum arquivo foi selecionado.");
            return;
        }

        setObjFilePath(path);

        // Tentar lançar aplicação sem thread separada
        try {
            Application.launch(OBJFileViewer.class);
        } catch (IllegalStateException e) {
            // JavaFX já iniciado → criar nova instância no thread JavaFX
            javafx.application.Platform.runLater(() -> {
                try {
                    OBJFileViewer viewer = new OBJFileViewer();
                    Stage stage = new Stage();
                    viewer.start(stage);
                } catch (Exception ex) {
                    System.out.println("Erro ao criar viewer: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("Erro geral: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (objFilePath == null) {
            System.out.println("Nenhum arquivo OBJ selecionado.");
            return;
        }

        // Configurar interface principal
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

        // Configurar controles
        setupMouseControls(subScene);
        setupKeyboardControls(scene);

        // Iniciar timer de movimento apenas após mostrar a janela
        primaryStage.setOnShown(event -> startMovementTimer());

        // Carregar modelo OBJ
        loadOBJModel();

        // Configurar janela
        primaryStage.setTitle("Visualizador OBJ 3D - " + objFilePath);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Focar na cena para receber eventos de teclado
        subScene.requestFocus();

        updateInfoLabel();
    }

    /**
     * Carrega o modelo OBJ e calcula seu bounding box para posicionamento automático da câmera
     */
    private void loadOBJModel() {
        try {
            loadedModel = OBJLoader.loadObj(objFilePath);
            sceneRoot.getChildren().add(loadedModel);

            // Calcular bounding box e posicionar câmera automaticamente
            calculateModelBounds();
            positionCameraForModel();

            infoLabel.setText("Modelo carregado: " + objFilePath);
        } catch (Exception e) {
            infoLabel.setText("Erro ao carregar modelo: " + e.getMessage());
            System.out.println("Erro detalhado: " + e.toString());
            e.printStackTrace();
            // Não interromper a execução mesmo com erro
        }
    }

    /**
     * Calcula o bounding box do modelo carregado
     */
    private void calculateModelBounds() {
        if (loadedModel != null) {
            Bounds bounds = loadedModel.getBoundsInLocal();

            // Calcular centro do modelo
            modelCenter = new Point3D(
                    bounds.getCenterX(),
                    bounds.getCenterY(),
                    bounds.getCenterZ()
            );

            // Calcular raio do modelo (maior dimensão)
            modelRadius = Math.max(
                    Math.max(bounds.getWidth(), bounds.getHeight()),
                    bounds.getDepth()
            ) / 2.0;

            // Garantir um raio mínimo
            if (modelRadius < 0.1) {
                modelRadius = 1.0;
            }
        }
    }

    /**
     * Posiciona a câmera automaticamente para visualizar o modelo completo
     */
    private void positionCameraForModel() {
        // Distância ideal da câmera baseada no raio do modelo
        double idealDistance = modelRadius * 3.0;

        // Posicionar câmera em frente ao modelo
        cameraPosition = new Point3D(
                modelCenter.getX(),
                modelCenter.getY(),
                modelCenter.getZ() + idealDistance
        );

        // Resetar rotação da câmera
        cameraYaw = 0;
        cameraPitch = 0;

        updateCameraTransform();
    }

    /**
     * Configura a câmera perspectiva
     */
    private void setupCamera() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(60);
    }

    /**
     * Configura iluminação da cena
     */
    private void setupLights() {
        // Luz ambiente
        AmbientLight ambient = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        sceneRoot.getChildren().add(ambient);

        // Luz principal
        PointLight mainLight = new PointLight(Color.WHITE);
        mainLight.setTranslateX(modelCenter.getX() + modelRadius);
        mainLight.setTranslateY(modelCenter.getY() - modelRadius);
        mainLight.setTranslateZ(modelCenter.getZ() + modelRadius);
        sceneRoot.getChildren().add(mainLight);

        // Luz de preenchimento
        PointLight fillLight = new PointLight(Color.color(0.3, 0.3, 0.5));
        fillLight.setTranslateX(modelCenter.getX() - modelRadius);
        fillLight.setTranslateY(modelCenter.getY() + modelRadius/2);
        fillLight.setTranslateZ(modelCenter.getZ() - modelRadius);
        sceneRoot.getChildren().add(fillLight);
    }

    /**
     * Configura controles do mouse
     */
    private void setupMouseControls(SubScene subScene) {
        subScene.setOnMousePressed(this::onMousePressed);
        subScene.setOnMouseDragged(this::onMouseDragged);
        subScene.setOnMouseReleased(this::onMouseReleased);
        subScene.setOnScroll(this::onScroll);
        subScene.setOnMouseClicked(this::onMouseClicked);

        // Garantir que a SubScene possa receber foco
        subScene.setFocusTraversable(true);
    }

    /**
     * Configura controles de teclado WASD
     */
    private void setupKeyboardControls(Scene scene) {
        scene.setOnKeyPressed(this::onKeyPressed);
        scene.setOnKeyReleased(this::onKeyReleased);
    }

    /**
     * Inicia o timer de movimento para controles suaves WASD
     * Timer é iniciado apenas quando a janela estiver visível
     */
    private void startMovementTimer() {
        if (movementTimer != null) {
            return; // Evita criar múltiplos timers
        }

        movementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleMovement();
            }
        };
        movementTimer.start();
    }

    /**
     * Processa movimento contínuo baseado nas teclas pressionadas
     * Protegido contra erros que possam causar falha
     */
    private void handleMovement() {
        try {
            if (pressedKeys.isEmpty()) return;

            // Calcular vetores de movimento baseados na rotação da câmera
            double yawRad = Math.toRadians(cameraYaw);
            double pitchRad = Math.toRadians(cameraPitch);

            // Vetores direcionais da câmera
            Point3D forward = new Point3D(
                    Math.sin(yawRad) * Math.cos(pitchRad),
                    -Math.sin(pitchRad),
                    -Math.cos(yawRad) * Math.cos(pitchRad)
            );

            Point3D right = new Point3D(
                    Math.cos(yawRad),
                    0,
                    Math.sin(yawRad)
            );

            Point3D up = new Point3D(0, 1, 0);

            // Calcular movimento baseado nas teclas pressionadas
            Point3D movement = new Point3D(0, 0, 0);
            double speed = CAMERA_SPEED * modelRadius; // Escalar velocidade com o modelo

            if (pressedKeys.contains(KeyCode.W)) {
                movement = movement.add(forward.multiply(speed));
            }
            if (pressedKeys.contains(KeyCode.S)) {
                movement = movement.add(forward.multiply(-speed));
            }
            if (pressedKeys.contains(KeyCode.A)) {
                movement = movement.add(right.multiply(-speed));
            }
            if (pressedKeys.contains(KeyCode.D)) {
                movement = movement.add(right.multiply(speed));
            }
            if (pressedKeys.contains(KeyCode.Q)) {
                movement = movement.add(up.multiply(speed));
            }
            if (pressedKeys.contains(KeyCode.E)) {
                movement = movement.add(up.multiply(-speed));
            }

            // Aplicar movimento
            if (movement.magnitude() > 0) {
                cameraPosition = cameraPosition.add(movement);
                updateCameraTransform();
            }
        } catch (Exception e) {
            // Log do erro mas não interromper o timer
            System.out.println("Erro no movimento: " + e.getMessage());
        }
    }

    /**
     * Eventos de mouse
     */
    private void onMousePressed(MouseEvent e) {
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
        mousePressed = true;

        // Garantir foco para receber eventos de teclado
        ((Node) e.getSource()).requestFocus();
    }

    private void onMouseDragged(MouseEvent e) {
        if (!mousePressed) return;

        double deltaX = e.getSceneX() - lastMouseX;
        double deltaY = e.getSceneY() - lastMouseY;
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();

        if (e.isPrimaryButtonDown()) {
            // Rotação da câmera com o mouse
            cameraYaw = (cameraYaw + deltaX * MOUSE_SENSITIVITY) % 360;
            cameraPitch = clamp(cameraPitch + deltaY * MOUSE_SENSITIVITY, MIN_PITCH, MAX_PITCH);
            updateCameraTransform();
        }
    }

    private void onMouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    private void onScroll(javafx.scene.input.ScrollEvent e) {
        // Zoom usando scroll (movimento para frente/trás)
        double deltaY = e.getDeltaY();
        double yawRad = Math.toRadians(cameraYaw);
        double pitchRad = Math.toRadians(cameraPitch);

        Point3D forward = new Point3D(
                Math.sin(yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRad),
                -Math.cos(yawRad) * Math.cos(pitchRad)
        );

        double zoomDistance = deltaY * ZOOM_SPEED * modelRadius;
        cameraPosition = cameraPosition.add(forward.multiply(zoomDistance));
        updateCameraTransform();
    }

    private void onMouseClicked(MouseEvent e) {
        // Adicionar esfera para medição (Shift + botão direito)
        if (e.isSecondaryButtonDown() && e.isShiftDown()) {
            PickResult pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode() != null) {
                Point3D worldPoint = pickResult.getIntersectedPoint();
                addSphereAt(worldPoint);
            }
        }
        // Rotação automática do objeto (duplo clique esquerdo)
        else if (e.getClickCount() == 2 && e.isPrimaryButtonDown()) {
            PickResult pickResult = e.getPickResult();
            Node pickedNode = pickResult.getIntersectedNode();
            if (pickedNode != null && pickedNode != sceneRoot) {
                rotateObject(pickedNode);
            }
        }
    }

    /**
     * Eventos de teclado
     */
    private void onKeyPressed(KeyEvent e) {
        pressedKeys.add(e.getCode());

        // Teclas especiais
        if (e.getCode() == KeyCode.R) {
            // Reset da câmera
            positionCameraForModel();
        } else if (e.getCode() == KeyCode.C) {
            // Limpar pontos de medição
            clearMeasurementPoints();
        }
    }

    private void onKeyReleased(KeyEvent e) {
        pressedKeys.remove(e.getCode());
    }

    /**
     * Atualiza a transformação da câmera
     */
    private void updateCameraTransform() {
        // Posicionar câmera
        camera.setTranslateX(cameraPosition.getX());
        camera.setTranslateY(cameraPosition.getY());
        camera.setTranslateZ(cameraPosition.getZ());

        // Aplicar rotações
        camera.getTransforms().clear();
        camera.getTransforms().add(new Rotate(-cameraPitch, Rotate.X_AXIS));
        camera.getTransforms().add(new Rotate(-cameraYaw, Rotate.Y_AXIS));
    }

    /**
     * Sistema de medição - adiciona esfera no ponto clicado
     */
    private void addSphereAt(Point3D point) {
        double sphereSize = modelRadius * 0.02; // Esfera proporcional ao modelo
        Sphere sphere = new Sphere(sphereSize);
        PhongMaterial material = new PhongMaterial(Color.RED);
        sphere.setMaterial(material);
        sphere.setTranslateX(point.getX());
        sphere.setTranslateY(point.getY());
        sphere.setTranslateZ(point.getZ());

        sceneRoot.getChildren().add(sphere);
        spheres.add(sphere);

        updateDistanceInfo();
    }

    /**
     * Remove todos os pontos de medição
     */
    private void clearMeasurementPoints() {
        for (Sphere sphere : spheres) {
            sceneRoot.getChildren().remove(sphere);
        }
        spheres.clear();
        updateInfoLabel();
    }

    /**
     * Atualiza informações de distância entre pontos
     */
    private void updateDistanceInfo() {
        if (spheres.size() >= 2) {
            Sphere a = spheres.get(spheres.size() - 2);
            Sphere b = spheres.get(spheres.size() - 1);
            double dist = calculateDistance(a, b);
            updateInfoLabel();
        } else {
            updateInfoLabel();
        }
    }

    /**
     * Calcula distância entre duas esferas
     */
    private double calculateDistance(Sphere a, Sphere b) {
        return Math.sqrt(
                Math.pow(a.getTranslateX() - b.getTranslateX(), 2) +
                        Math.pow(a.getTranslateY() - b.getTranslateY(), 2) +
                        Math.pow(a.getTranslateZ() - b.getTranslateZ(), 2)
        );
    }

    /**
     * Inicia rotação automática de um objeto
     */
    private void rotateObject(Node node) {
        if (currentRotation != null) {
            currentRotation.stop();
        }

        currentRotation = new RotateTransition(Duration.seconds(3), node);
        currentRotation.setAxis(Rotate.Y_AXIS);
        currentRotation.setByAngle(360);
        currentRotation.setCycleCount(Animation.INDEFINITE);
        currentRotation.play();
    }

    /**
     * Atualiza o texto informativo
     */
    private void updateInfoLabel() {
        StringBuilder info = new StringBuilder();

        if (spheres.size() >= 2) {
            Sphere a = spheres.get(spheres.size() - 2);
            Sphere b = spheres.get(spheres.size() - 1);
            double dist = calculateDistance(a, b);
            info.append(String.format("Última distância: %.4f | ", dist));
        }

        info.append(String.format("Pontos: %d | ", spheres.size()));
        info.append("WASD+QE: Mover | Mouse: Rotacionar | Shift+Click direito: Medir | R: Reset | C: Limpar");

        infoLabel.setText(info.toString());
    }

    /**
     * Utilitário para limitar valores
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Método stop() removido para evitar fechamento automático
}
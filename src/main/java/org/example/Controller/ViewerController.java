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
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.Util.OBJLoader;

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
 * - Sistema de medição com pontos múltiplos
 * - Linhas conectando os pontos de medição
 * - Controles WASD para movimento
 * - Cálculo de distâncias entre pontos e distância acumulada
 * - Exibição de informações na interface
 */
public class ViewerController implements Initializable {

    @FXML private StackPane canvasContainer;
    @FXML private ToggleButton togglePlace;
    @FXML private Label lblInfo;
    @FXML private Label lblMeasurements; // Nova label para medições

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

    // 🚀 VELOCIDADES AUMENTADAS SIGNIFICATIVAMENTE
    private final double CAMERA_SPEED = 0.3;        // 6x mais rápido que antes (era 0.05)
    private final double CAMERA_SPEED_FAST = 0.8;        // Velocidade com Shift pressionado
    private final double MOUSE_SENSITIVITY = 1.2;        // 2.4x mais sensível (era 0.5)
    private final double ZOOM_SPEED = 0.15;              // 1.5x mais rápido no zoom

    // ⚡ CONFIGURAÇÕES DE ACELERAÇÃO
    private double currentSpeedMultiplier = 1.0;         // Multiplicador dinâmico de velocidade
    private final double ACCELERATION_RATE = 0.05;       // Taxa de aceleração por frame
    private final double MAX_ACCELERATION = 2.5;         // Máxima aceleração possível

    // Controles de entrada
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private double lastMouseX, lastMouseY;
    private boolean mousePressed = false;

    // Sistema de medição com múltiplas esferas e linhas
    private List<Sphere> spheres = new ArrayList<>();              // Todas as esferas
    private List<Point3D> measurementPoints = new ArrayList<>();   // Pontos de medição
    private List<Cylinder> connectionLines = new ArrayList<>();    // Linhas conectando os pontos
    private RotateTransition currentRotation;
    private boolean placeModeEnabled = false;

    // Timer para movimento suave
    private AnimationTimer movementTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupScene3D();
        updateInfoLabel();
        updateMeasurementsLabel();
    }

    /**
     * Carrega um modelo automaticamente na inicialização (chamado pelo LoginController)
     */
    public void loadModelOnStartup(String objPath) {
        if (objPath != null && !objPath.isEmpty()) {
            // Aguardar a cena estar totalmente carregada antes de carregar o modelo
            javafx.application.Platform.runLater(() -> {
                loadOBJModel(objPath);
            });
        }
    }

    /**
     * Configura a cena 3D inicial
     */
    private void setupScene3D() {
        // Criar cena 3D
        sceneRoot = new Group();
        setupCamera();
        setupLights();

        // Criar SubScene que se ajusta automaticamente ao container
        subScene = new SubScene(sceneRoot, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#2b2b2b"));
        subScene.setCamera(camera);
        subScene.setFocusTraversable(true);

        // Adicionar ao container
        canvasContainer.getChildren().add(subScene);

        // Bind para redimensionar com o container
        subScene.widthProperty().bind(canvasContainer.widthProperty());
        subScene.heightProperty().bind(canvasContainer.heightProperty());

        // Configurar controles
        setupMouseControls();
        setupKeyboardControls();

        // Iniciar timer de movimento
        startMovementTimer();
    }

    /**
     * Configura a câmera perspectiva
     */
    private void setupCamera() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(60);
        updateCameraTransform();
    }

    /**
     * Configura iluminação dinâmica da cena
     */
    private void setupLights() {
        // Limpar luzes existentes (AmbientLight e PointLight são subclasses de LightBase)
        sceneRoot.getChildren().removeIf(node ->
                node instanceof AmbientLight ||
                        node instanceof PointLight ||
                        node instanceof DirectionalLight
        );

        // Luz ambiente
        AmbientLight ambient = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        sceneRoot.getChildren().add(ambient);

        // Luz principal posicionada dinamicamente
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
     * Configura controles do mouse com BOTÃO DO SCROLL para marcação de pontos
     */
    private void setupMouseControls() {
        subScene.setOnMousePressed(this::onMousePressed);
        subScene.setOnMouseDragged(this::onMouseDragged);
        subScene.setOnMouseReleased(this::onMouseReleased);
        subScene.setOnScroll(this::onScroll);

        // Modificado: usar addEventHandler para permitir múltiplos cliques
        subScene.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (placeModeEnabled && event.getButton() == MouseButton.PRIMARY) {
                Point3D point = event.getPickResult().getIntersectedPoint();
                if (point != null && event.getPickResult().getIntersectedNode() != null) {
                    // Ignora cliques em esferas já criadas
                    if (!(event.getPickResult().getIntersectedNode() instanceof Sphere)) {
                        addSphereAt(point);
                    }
                }
            }
            // Botão do meio do mouse sempre adiciona pontos
            else if (event.getButton() == MouseButton.MIDDLE) {
                Point3D point = event.getPickResult().getIntersectedPoint();
                if (point != null && event.getPickResult().getIntersectedNode() != null) {
                    // Ignora cliques em esferas já criadas
                    if (!(event.getPickResult().getIntersectedNode() instanceof Sphere)) {
                        addSphereAt(point);
                    }
                }
            }
        });
    }

    /**
     * Configura controles de teclado WASD
     */
    private void setupKeyboardControls() {
        // Aplicar eventos ao container principal para capturar teclas
        canvasContainer.setOnKeyPressed(this::onKeyPressed);
        canvasContainer.setOnKeyReleased(this::onKeyReleased);
        canvasContainer.setFocusTraversable(true);

        // Também aplicar à SubScene
        subScene.setOnKeyPressed(this::onKeyPressed);
        subScene.setOnKeyReleased(this::onKeyReleased);
    }

    /**
     * Inicia o timer de movimento suave
     */
    private void startMovementTimer() {
        if (movementTimer != null) {
            return;
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
     * Processa movimento contínuo WASD (movimento da câmera no espaço orbital)
     */
    private void handleMovement() {
        try {
            if (pressedKeys.isEmpty()) return;

            // Calcular vetores baseados na orientação atual da câmera
            double yawRad = Math.toRadians(cameraYaw);
            double pitchRad = Math.toRadians(cameraPitch);

            // Vetores direcionais para movimento do target
            Point3D forward = new Point3D(
                    Math.sin(yawRad),
                    0,
                    -Math.cos(yawRad)
            );

            Point3D right = new Point3D(
                    Math.cos(yawRad),
                    0,
                    Math.sin(yawRad)
            );

            Point3D up = new Point3D(0, 1, 0);

            Point3D movement = new Point3D(0, 0, 0);
            double speed = CAMERA_SPEED * (cameraDistance / 5.0); // Velocidade proporcional à distância

            // Movimento do target (ponto focal)
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
                movement = movement.add(up.multiply(-speed));
            }
            if (pressedKeys.contains(KeyCode.E)) {
                movement = movement.add(up.multiply(speed));
            }

            if (movement.magnitude() > 0) {
                cameraTarget = cameraTarget.add(movement);
                updateCameraTransform();
            }
        } catch (Exception e) {
            System.out.println("Erro no movimento: " + e.getMessage());
        }
    }

    // =================== EVENTOS DE MOUSE (CONTROLES INVERTIDOS) ===================

    private void onMousePressed(MouseEvent e) {
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
        mousePressed = true;

        // 🎯 BOTÃO DO SCROLL (MIDDLE BUTTON) PARA ADICIONAR PONTOS
        if (e.isMiddleButtonDown()) {
            // Picking otimizado para resposta rápida
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
            // Rotação orbital ao redor do target
            cameraYaw = (cameraYaw - deltaX * MOUSE_SENSITIVITY) % 360;
            cameraPitch = clamp(cameraPitch - deltaY * MOUSE_SENSITIVITY, MIN_PITCH, MAX_PITCH);
            updateCameraTransform();

        } else if (e.isPrimaryButtonDown() && e.isShiftDown()) {
            // Pan: mover o target da câmera
            double yawRad = Math.toRadians(cameraYaw);
            Point3D right = new Point3D(Math.cos(yawRad), 0, Math.sin(yawRad));
            Point3D up = new Point3D(0, 1, 0);

            double panSpeed = 0.01 * cameraDistance;
            Point3D panMovement = right.multiply(-deltaX * panSpeed)
                    .add(up.multiply(deltaY * panSpeed));

            cameraTarget = cameraTarget.add(panMovement);
            updateCameraTransform();

        } else if (e.isSecondaryButtonDown()) {
            // Zoom com botão direito
            cameraDistance = Math.max(MIN_DISTANCE, cameraDistance + deltaY * 0.01);
            updateCameraTransform();
        }
    }

    private void onMouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    private void onScroll(javafx.scene.input.ScrollEvent e) {
        // Zoom suave com scroll mais rápido
        double delta = e.getDeltaY();
        double zoomFactor = delta > 0 ? 0.85 : 1.15; // Zoom mais agressivo (era 0.9/1.1)

        cameraDistance = Math.max(MIN_DISTANCE, cameraDistance * zoomFactor);
        updateCameraTransform();
    }

    // =================== EVENTOS DE TECLADO ===================

    private void onKeyPressed(KeyEvent e) {
        pressedKeys.add(e.getCode());

        // Teclas especiais com velocidades otimizadas
        if (e.getCode() == KeyCode.R) {
            resetCamera();
        } else if (e.getCode() == KeyCode.C) {
            onClearSpheres();
        }
        // 🚀 NOVA FUNCIONALIDADE: Tecla de velocidade adicional
        else if (e.getCode() == KeyCode.CONTROL) {
            // Ctrl para velocidade ainda maior (combinável com Shift)
            System.out.println("Modo super velocidade ativado");
        }
    }

    private void onKeyReleased(KeyEvent e) {
        pressedKeys.remove(e.getCode());
    }

    // =================== MÉTODOS DOS BOTÕES FXML ===================

    /**
     * Botão: Abrir modelo OBJ
     */
    @FXML
    private void onOpenModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar arquivo OBJ");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos OBJ", "*.obj")
        );

        Stage stage = (Stage) canvasContainer.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            loadOBJModel(selectedFile.getAbsolutePath());
        }
    }

    /**
     * Botão: Alternar modo colocar bolinha
     */
    @FXML
    private void onTogglePlace() {
        placeModeEnabled = togglePlace.isSelected();
        updateInfoLabel();
    }

    /**
     * Botão: Adicionar bolinha na frente da câmera
     */
    @FXML
    private void onAddSphere() {
        // Calcular posição na direção do target, mas mais próxima da câmera
        Point3D cameraPos = new Point3D(
                camera.getTranslateX(),
                camera.getTranslateY(),
                camera.getTranslateZ()
        );

        // Direção da câmera para o target
        Point3D direction = cameraTarget.subtract(cameraPos).normalize();

        // Posição a uma distância fixa da câmera
        double distanceFromCamera = cameraDistance * 0.3;
        Point3D spherePosition = cameraPos.add(direction.multiply(distanceFromCamera));

        addSphereAt(spherePosition);
    }

    /**

     * Botão: Limpar todas as bolinhas e linhas
     */
    @FXML
    private void onClearSpheres() {
        // Remover todas as esferas da cena
        for (Sphere sphere : spheres) {
            sceneRoot.getChildren().remove(sphere);
        }

        // Remover todas as linhas da cena
        for (Cylinder line : connectionLines) {
            sceneRoot.getChildren().remove(line);
        }

        // Limpar listas
        spheres.clear();
        measurementPoints.clear();
        connectionLines.clear();

        // Atualizar interface
        updateInfoLabel();
        updateMeasurementsLabel();
    }

    // =================== CARREGAMENTO DE MODELO ===================

    /**
     * Carrega modelo OBJ usando OBJLoader
     */
    private void loadOBJModel(String path) {
        try {
            System.out.println("Carregando modelo: " + path);

            // Remover modelo anterior se existir
            if (loadedModel != null) {
                sceneRoot.getChildren().remove(loadedModel);
            }

            // Carregar novo modelo
            Group model = OBJLoader.loadObj(path);
            if (model.getChildren().isEmpty()) {
                lblInfo.setText("Erro: Modelo vazio ou inválido");
                return;
            }

            loadedModel = model;
            sceneRoot.getChildren().add(loadedModel);

            // Calcular bounds e reposicionar câmera
            calculateModelBounds();
            setupLights(); // Reconfigurar luzes baseadas no modelo
            resetCamera();

            lblInfo.setText("Modelo carregado: " + new File(path).getName());
            System.out.println("Modelo carregado com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar modelo: " + e.getMessage());
            e.printStackTrace();
            lblInfo.setText("Erro ao carregar modelo: " + e.getMessage());
        }
    }

    /**
     * Calcula bounds do modelo carregado
     */
    private void calculateModelBounds() {
        if (loadedModel != null) {
            Bounds bounds = loadedModel.getBoundsInLocal();

            modelCenter = new Point3D(
                    bounds.getCenterX(),
                    bounds.getCenterY(),
                    bounds.getCenterZ()
            );

            modelRadius = Math.max(
                    Math.max(bounds.getWidth(), bounds.getHeight()),
                    bounds.getDepth()
            ) / 2.0;

            if (modelRadius < 0.1) {
                modelRadius = 1.0;
            }
        }
    }

    /**
     * Reposiciona câmera para ver o modelo completo (sistema orbital)
     */
    private void resetCamera() {
        // Definir target como centro do modelo
        cameraTarget = modelCenter;

        // Definir distância ideal baseada no tamanho do modelo
        cameraDistance = modelRadius * 3.0;

        // Posição inicial: ligeiramente acima e na frente
        cameraYaw = 45;     // 45 graus à direita
        cameraPitch = 20;   // 20 graus acima

        updateCameraTransform();
    }

    /**
     * Atualiza transformação da câmera (sistema orbital)
     */
    private void updateCameraTransform() {
        // Calcular posição da câmera baseada em coordenadas esféricas
        double yawRad = Math.toRadians(cameraYaw);
        double pitchRad = Math.toRadians(cameraPitch);

        double x = cameraTarget.getX() + cameraDistance * Math.cos(pitchRad) * Math.sin(yawRad);
        double y = cameraTarget.getY() + cameraDistance * Math.sin(pitchRad);
        double z = cameraTarget.getZ() + cameraDistance * Math.cos(pitchRad) * Math.cos(yawRad);

        // Posicionar câmera
        camera.setTranslateX(x);
        camera.setTranslateY(y);
        camera.setTranslateZ(z);

        // Fazer câmera olhar para o target
        camera.getTransforms().clear();

        // Calcular vetor "look at"
        Point3D lookDirection = cameraTarget.subtract(x, y, z).normalize();

        // Aplicar rotações para olhar para o target
        double targetYaw = Math.toDegrees(Math.atan2(lookDirection.getX(), lookDirection.getZ()));
        double targetPitch = Math.toDegrees(Math.asin(-lookDirection.getY()));

        camera.getTransforms().add(new Rotate(-targetPitch, Rotate.X_AXIS));
        camera.getTransforms().add(new Rotate(-targetYaw, Rotate.Y_AXIS));
    }

    /**
     * Adiciona esfera de medição e linha conectora
     */
    private void addSphereAt(Point3D point) {
        double sphereSize = Math.max(modelRadius * 0.015, 0.02);

        // Criar esfera
        Sphere sphere = new Sphere(sphereSize);
        PhongMaterial sphereMaterial = new PhongMaterial(Color.RED);
        sphere.setMaterial(sphereMaterial);
        sphere.setTranslateX(point.getX());
        sphere.setTranslateY(point.getY());
        sphere.setTranslateZ(point.getZ());

        sceneRoot.getChildren().add(sphere);
        spheres.add(sphere);
        measurementPoints.add(point);

        // Criar linha conectora se houver pelo menos 2 pontos
        if (measurementPoints.size() >= 2) {
            Point3D previousPoint = measurementPoints.get(measurementPoints.size() - 2);
            Point3D currentPoint = measurementPoints.get(measurementPoints.size() - 1);

            Cylinder connectionLine = createLineBetweenPoints(previousPoint, currentPoint);
            sceneRoot.getChildren().add(connectionLine);
            connectionLines.add(connectionLine);
        }

        javafx.application.Platform.runLater(() -> {
            updateInfoLabel();
            updateMeasurementsLabel();
        });
    }

    /**
     * Cria uma linha (cilindro) entre dois pontos 3D com orientação correta
     */
    private Cylinder createLineBetweenPoints(Point3D start, Point3D end) {
        // Calcular vetor direção e distância
        Point3D direction = end.subtract(start);
        double distance = direction.magnitude();

        // Criar cilindro (linha) com altura = distância entre pontos
        double lineRadius = Math.max(modelRadius * 0.002, 0.005); // Linha fina
        Cylinder line = new Cylinder(lineRadius, distance);

        // Material da linha (azul semi-transparente)
        PhongMaterial lineMaterial = new PhongMaterial(Color.CYAN);
        line.setMaterial(lineMaterial);

        // Posicionar no ponto médio entre start e end
        Point3D midPoint = start.add(end).multiply(0.5);
        line.setTranslateX(midPoint.getX());
        line.setTranslateY(midPoint.getY());
        line.setTranslateZ(midPoint.getZ());

        // CORREÇÃO: Alinhar cilindro com o vetor entre os pontos
        // O cilindro padrão está no eixo Y, precisamos rotacioná-lo para o vetor direção
        Point3D yAxis = new Point3D(0, 1, 0); // Eixo Y padrão do cilindro
        Point3D unitDirection = direction.normalize();

        // Calcular o eixo de rotação (produto vetorial)
        Point3D rotationAxis = yAxis.crossProduct(unitDirection);

        // Calcular ângulo entre os vetores
        double dotProduct = yAxis.dotProduct(unitDirection);
        double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dotProduct))));

        // Aplicar rotação apenas se necessário (evitar rotação de 0°)
        if (rotationAxis.magnitude() > 0.001 && Math.abs(angle) > 0.1) {
            Rotate rotation = new Rotate(angle, rotationAxis);
            line.getTransforms().add(rotation);
        }
        // Se os vetores são opostos (180°), rotacionar 180° no eixo X
        else if (Math.abs(angle - 180) < 0.1) {
            line.getTransforms().add(new Rotate(180, Rotate.X_AXIS));
        }

        return line;
    }

    /**
     * Calcula a distância total acumulada entre todos os pontos na ordem
     */
    private double calculateTotalDistance() {
        if (measurementPoints.size() < 2) {
            return 0.0;
        }

        double total = 0.0;
        for (int i = 1; i < measurementPoints.size(); i++) {
            Point3D previous = measurementPoints.get(i - 1);
            Point3D current = measurementPoints.get(i);
            total += calculateDistance(previous, current);
        }
        return total;
    }

    /**
     * Rotaciona objeto automaticamente
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
     * Atualiza informações na interface principal
     */
    private void updateInfoLabel() {
        StringBuilder info = new StringBuilder();

        // Adicionar informações de modo
        if (placeModeEnabled) {
            info.append("🎯 MODO COLOCAR: Clique para adicionar pontos");
        } else {
            info.append("🖱️ MODO NORMAL: Duplo clique para rotacionar");
        }
        info.append(" | 🔘 Botão do scroll: Adicionar ponto rápido");

        lblInfo.setText(info.toString());
    }

    /**
     * Atualiza informações de medições na área dedicada
     */
    private void updateMeasurementsLabel() {
        if (lblMeasurements == null) return; // Proteção para FXML antigo

        StringBuilder measurements = new StringBuilder();

        if (measurementPoints.isEmpty()) {
            measurements.append("Nenhuma medição ativa");
        } else if (measurementPoints.size() == 1) {
            measurements.append("1 ponto marcado - Adicione mais pontos para medir distâncias");
        } else {
            // Distância entre os dois últimos pontos
            Point3D lastPoint = measurementPoints.get(measurementPoints.size() - 1);
            Point3D previousPoint = measurementPoints.get(measurementPoints.size() - 2);
            double lastDistance = calculateDistance(previousPoint, lastPoint);

            // Distância total acumulada
            double totalDistance = calculateTotalDistance();

            measurements.append(String.format("Última distância: %.3f unidades | ", lastDistance));
            measurements.append(String.format("Distância acumulada: %.3f unidades | ", totalDistance));
            measurements.append(String.format("Total de pontos: %d", measurementPoints.size()));
        }

        lblMeasurements.setText(measurements.toString());
    }

    /**
     * Calcula distância entre dois pontos 3D
     */
    private double calculateDistance(Point3D a, Point3D b) {
        return a.distance(b);
    }

    /**
     * Limita valor entre min e max
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @FXML
    private void onSendMeasurements() {
        if (measurementPoints.isEmpty()) {
            lblInfo.setText(" Nenhuma medição para enviar.");
            return;
        }

        try {
            // Montar JSON com as distâncias e pontos
            JSONArray pointsArray = new JSONArray();
            for (Point3D p : measurementPoints) {
                JSONObject pointJson = new JSONObject();
                pointJson.put("x", p.getX());
                pointJson.put("y", p.getY());
                pointJson.put("z", p.getZ());
                pointsArray.put(pointJson);
            }

            JSONObject json = new JSONObject();
            json.put("totalDistance", calculateTotalDistance());
            json.put("points", pointsArray);

            // Enviar JSON para a API
            String apiUrl = "http://localhost:8080/api/medicoes"; // ⚠️ ajuste sua URL
            sendJsonToApi(apiUrl, json.toString());

            lblInfo.setText("✅ Dados enviados com sucesso!");

        } catch (Exception e) {
            lblInfo.setText("⚠️ Erro ao enviar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Envia um JSON via POST para uma API REST
     */
    private void sendJsonToApi(String apiUrl, String jsonBody) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Resposta da API: " + responseCode);
        conn.disconnect();
    }

}


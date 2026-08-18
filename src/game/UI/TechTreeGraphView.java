package game.UI;

import game.model.research.TechNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class TechTreeGraphView {
    private final List<TechNode> nodes;
    private final String title;
    private final boolean religionLocked;
    private final Stage stage;

    // Константы отрисовки
    private static final double NODE_WIDTH = 140;
    private static final double NODE_HEIGHT = 50;
    private static final double HORIZONTAL_SPACING = 180;
    private static final double VERTICAL_SPACING = 70;
    private static final double PADDING = 40;

    public TechTreeGraphView(List<TechNode> nodes, String title, boolean religionLocked) {
        this.nodes = new ArrayList<>(nodes);
        this.title = title;
        this.religionLocked = religionLocked;
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
    }

    public void show() {
        // Если религия заблокирована и это ветка религии – показываем сообщение
        if (religionLocked && title.contains("Религия")) {
            VBox lockBox = new VBox(15);
            lockBox.setAlignment(Pos.CENTER);
            lockBox.setPadding(new Insets(20));
            Label lockLabel = new Label("🔒 Религия заблокирована");
            lockLabel.setTextFill(Color.RED);
            lockLabel.setFont(Font.font(18));
            Label hint = new Label("Изучите 'Высшая воля' в культуре, чтобы открыть религиозную ветку.");
            hint.setTextFill(Color.LIGHTGRAY);
            Button closeBtn = new Button("Закрыть");
            closeBtn.setOnAction(e -> stage.close());
            lockBox.getChildren().addAll(lockLabel, hint, closeBtn);
            Scene lockScene = new Scene(lockBox, 400, 200);
            stage.setScene(lockScene);
            stage.showAndWait();
            return;
        }

        // Вычисляем уровни (глубину) для каждого узла
        Map<String, Integer> depthMap = new HashMap<>();
        for (TechNode node : nodes) {
            depthMap.put(node.getName(), calculateDepth(node, new HashSet<>()));
        }

        // Группируем узлы по глубине
        Map<Integer, List<TechNode>> levels = new TreeMap<>();
        for (TechNode node : nodes) {
            int depth = depthMap.get(node.getName());
            levels.computeIfAbsent(depth, k -> new ArrayList<>()).add(node);
        }

        // Определяем размеры Canvas
        int maxDepth = levels.isEmpty() ? 0 : levels.keySet().stream().max(Integer::compareTo).orElse(0);
        int maxNodesInLevel = levels.values().stream().mapToInt(List::size).max().orElse(1);
        double canvasWidth = (maxDepth + 1) * HORIZONTAL_SPACING + 2 * PADDING;
        double canvasHeight = maxNodesInLevel * (NODE_HEIGHT + VERTICAL_SPACING) + 2 * PADDING;

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(30, 30, 40));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Рисуем связи (линии со стрелками)
        for (TechNode node : nodes) {
            int fromDepth = depthMap.get(node.getName());
            double fromX = PADDING + fromDepth * HORIZONTAL_SPACING + NODE_WIDTH / 2;
            double fromY = getYPosition(node, levels, depthMap);
            for (String prereqName : node.getPrerequisites()) {
                TechNode prereq = findNodeByName(prereqName);
                if (prereq != null) {
                    int toDepth = depthMap.get(prereqName);
                    double toX = PADDING + toDepth * HORIZONTAL_SPACING + NODE_WIDTH / 2;
                    double toY = getYPosition(prereq, levels, depthMap);
                    drawArrow(gc, toX, toY, fromX, fromY, Color.rgb(180, 180, 200, 0.6));
                }
            }
        }

        // Рисуем узлы
        for (TechNode node : nodes) {
            int depth = depthMap.get(node.getName());
            double x = PADDING + depth * HORIZONTAL_SPACING;
            double y = getYPosition(node, levels, depthMap) - NODE_HEIGHT / 2;

            // Определяем цвет узла
            Color fillColor;
            Color textColor = Color.WHITE;
            if (node.isResearched()) {
                fillColor = Color.rgb(34, 139, 34); // зелёный
            } else {
                boolean available = true;
                for (String prereq : node.getPrerequisites()) {
                    TechNode p = findNodeByName(prereq);
                    if (p == null || !p.isResearched()) {
                        available = false;
                        break;
                    }
                }
                if (available) {
                    fillColor = Color.rgb(200, 180, 50); // жёлтый (доступно)
                } else {
                    fillColor = Color.rgb(80, 80, 90); // серый (заблокировано)
                    textColor = Color.LIGHTGRAY;
                }
            }

            // Рисуем прямоугольник узла
            gc.setFill(fillColor);
            gc.fillRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 8, 8);
            gc.setStroke(Color.rgb(200, 200, 200, 0.5));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 8, 8);

            // Текст
            gc.setFill(textColor);
            gc.setFont(Font.font(13));
            gc.fillText(node.getName(), x + 10, y + 22);
            gc.setFont(Font.font(10));
            gc.fillText("Cost: " + node.getCost(), x + 10, y + 40);
        }

        // Создаём окно с прокруткой
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setStyle("-fx-background: #1e1e2a; -fx-background-color: #1e1e2a;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2a;");
        root.setCenter(scrollPane);

        // Верхняя панель с заголовком и кнопкой закрытия
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER);
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font(18));
        Button closeBtn = new Button("Закрыть");
        closeBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #555; -fx-text-fill: white;");
        closeBtn.setOnAction(e -> stage.close());
        topBox.getChildren().addAll(titleLabel, closeBtn);
        root.setTop(topBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // --- Вспомогательные методы ---

    private int calculateDepth(TechNode node, Set<String> visited) {
        if (visited.contains(node.getName())) return 0; // защита от циклов
        visited.add(node.getName());
        int maxDepth = 0;
        for (String prereq : node.getPrerequisites()) {
            TechNode prereqNode = findNodeByName(prereq);
            if (prereqNode != null) {
                int d = calculateDepth(prereqNode, visited) + 1;
                if (d > maxDepth) maxDepth = d;
            }
        }
        return maxDepth;
    }

    private double getYPosition(TechNode node, Map<Integer, List<TechNode>> levels, Map<String, Integer> depthMap) {
        int depth = depthMap.get(node.getName());
        List<TechNode> levelNodes = levels.get(depth);
        int index = levelNodes.indexOf(node);
        double totalHeight = levelNodes.size() * (NODE_HEIGHT + VERTICAL_SPACING) - VERTICAL_SPACING;
        double startY = (totalHeight - (levelNodes.size() - 1) * (NODE_HEIGHT + VERTICAL_SPACING)) / 2;
        return PADDING + startY + index * (NODE_HEIGHT + VERTICAL_SPACING) + NODE_HEIGHT / 2;
    }

    private TechNode findNodeByName(String name) {
        for (TechNode n : nodes) {
            if (n.getName().equals(name)) return n;
        }
        return null;
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.strokeLine(x1, y1, x2, y2);

        // Стрелка на конце (от x1,y1 к x2,y2)
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowLength = 10;
        double arrowAngle = Math.PI / 6;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len > 5) {
            double x3 = x2 - arrowLength * Math.cos(angle - arrowAngle);
            double y3 = y2 - arrowLength * Math.sin(angle - arrowAngle);
            double x4 = x2 - arrowLength * Math.cos(angle + arrowAngle);
            double y4 = y2 - arrowLength * Math.sin(angle + arrowAngle);
            gc.setFill(color);
            gc.fillPolygon(new double[]{x2, x3, x4}, new double[]{y2, y3, y4}, 3);
        }
    }
}
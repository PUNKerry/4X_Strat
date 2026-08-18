package game.UI;

import game.controller.GameController;
import game.model.research.TechNode;
import game.model.research.TechTree;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class TechTreeInteractiveOverlay {
    private final List<TechNode> nodes;
    private final String title;
    private final GameController controller;
    private final StackPane overlayRoot;
    private final String branchType;

    private static final double NODE_WIDTH = 200;
    private static final double NODE_HEIGHT = 70;
    private static final double HORIZONTAL_SPACING = 240;
    private static final double VERTICAL_SPACING = 90;
    private static final double PADDING = 60;

    private final Map<TechNode, List<Line>> incomingLines = new HashMap<>();
    private final Map<TechNode, List<Line>> outgoingLines = new HashMap<>();
    private final Map<TechNode, StackPane> nodePanes = new HashMap<>();
    private Set<TechNode> visibleNodes = new HashSet<>();

    private final Map<TechNode, Boolean> nodeAvailability = new HashMap<>();
    private final Map<TechNode, Color> nodeDefaultFill = new HashMap<>();
    private final Map<TechNode, Color> nodeDefaultStroke = new HashMap<>();

    public TechTreeInteractiveOverlay(List<TechNode> nodes, String title,
                                      GameController controller,
                                      StackPane overlayRoot, String branchType) {
        this.nodes = new ArrayList<>(nodes);
        this.title = title;
        this.controller = controller;
        this.overlayRoot = overlayRoot;
        this.branchType = branchType;
    }

    public void show() {
        if (branchType.equals("religion") && !controller.isReligionUnlocked()) {
            showLockedMessage();
            return;
        }

        Map<String, Integer> depthMap = new HashMap<>();
        for (TechNode node : nodes) {
            depthMap.put(node.getName(), calculateDepth(node, new HashSet<>()));
        }

        Map<Integer, List<TechNode>> levels = new TreeMap<>();
        for (TechNode node : nodes) {
            int depth = depthMap.get(node.getName());
            levels.computeIfAbsent(depth, k -> new ArrayList<>()).add(node);
        }

        determineVisibleNodes();

        int maxDepth = levels.isEmpty() ? 0 : levels.keySet().stream().max(Integer::compareTo).orElse(0);
        int maxNodesInLevel = levels.values().stream().mapToInt(List::size).max().orElse(1);
        double treeWidth = (maxDepth + 1) * HORIZONTAL_SPACING + 2 * PADDING;
        double treeHeight = Math.max(maxNodesInLevel * (NODE_HEIGHT + VERTICAL_SPACING) + 2 * PADDING, 500);

        Pane treePane = new Pane();
        treePane.setPrefSize(treeWidth, treeHeight);
        treePane.setStyle("-fx-background-color: #1a1a2e;");

        // --- Рисуем линии ---
        incomingLines.clear();
        outgoingLines.clear();
        for (TechNode node : nodes) {
            incomingLines.put(node, new ArrayList<>());
            outgoingLines.put(node, new ArrayList<>());
        }

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
                    Color lineColor = (node.isResearched() && prereq.isResearched()) ?
                            Color.rgb(100, 220, 100, 0.8) :
                            Color.rgb(180, 180, 200, 0.4);
                    List<Line> lines = drawOrthogonalLine(treePane, fromX, fromY, toX, toY, lineColor);
                    incomingLines.get(node).addAll(lines);
                    outgoingLines.get(prereq).addAll(lines);
                }
            }
        }

        // --- Рисуем узлы ---
        nodePanes.clear();
        nodeAvailability.clear();
        nodeDefaultFill.clear();
        nodeDefaultStroke.clear();

        TechTree techTree = controller.getTechTree();

        for (TechNode node : nodes) {
            int depth = depthMap.get(node.getName());
            double x = PADDING + depth * HORIZONTAL_SPACING;
            double y = getYPosition(node, levels, depthMap) - NODE_HEIGHT / 2;

            boolean isVisible = visibleNodes.contains(node);
            boolean researched = node.isResearched();

            boolean isAvailable = false;
            if (!researched && isVisible) {
                boolean allPrereqsMet = true;
                for (String prereq : node.getPrerequisites()) {
                    if (!techTree.isResearched(prereq)) {
                        allPrereqsMet = false;
                        break;
                    }
                }
                if (allPrereqsMet) {
                    if (branchType.equals("social")) {
                        isAvailable = controller.getTurnManager().isSocialTechAvailable(node);
                    } else if (branchType.equals("religion")) {
                        // Религия разблокирована, если изучен Тотемизм – проверка уже сделана выше
                        isAvailable = true; // если мы дошли сюда, то религия разблокирована
                    } else {
                        isAvailable = true;
                    }
                }
            }

            final boolean isNodeAvailable = isAvailable;
            final boolean isResearched = researched;
            final boolean isNodeVisible = isVisible;
            final TechNode currentNode = node;

            Color fillColor, textColor, borderColor;
            String displayName = node.getName();

            if (!isVisible) {
                fillColor = Color.rgb(50, 50, 60);
                borderColor = Color.rgb(70, 70, 80);
                textColor = Color.rgb(150, 150, 160);
                displayName = "?";
            } else if (isResearched) {
                fillColor = Color.rgb(46, 160, 67);
                borderColor = Color.rgb(34, 120, 50);
                textColor = Color.WHITE;
            } else if (isNodeAvailable) {
                fillColor = Color.rgb(230, 200, 70);
                borderColor = Color.rgb(180, 150, 40);
                textColor = Color.BLACK;
            } else {
                fillColor = Color.rgb(80, 80, 90);
                borderColor = Color.rgb(60, 60, 70);
                textColor = Color.LIGHTGRAY;
            }

            nodeDefaultFill.put(node, fillColor);
            nodeDefaultStroke.put(node, borderColor);

            StackPane nodePane = new StackPane();
            nodePane.setLayoutX(x);
            nodePane.setLayoutY(y);
            nodePane.setPrefSize(NODE_WIDTH, NODE_HEIGHT);

            Rectangle bg = new Rectangle(NODE_WIDTH, NODE_HEIGHT);
            bg.setArcWidth(10);
            bg.setArcHeight(10);
            bg.setFill(fillColor);
            bg.setStroke(borderColor);
            bg.setStrokeWidth(2);
            bg.setEffect(new javafx.scene.effect.DropShadow(6, Color.rgb(0, 0, 0, 0.5)));

            Label nameLabel = new Label(displayName);
            nameLabel.setTextFill(textColor);
            nameLabel.setFont(Font.font(15));
            nameLabel.setWrapText(true);
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            nameLabel.setMaxWidth(NODE_WIDTH - 10);

            Label costLabel = new Label(isVisible ? "⚙" + node.getCost() : "");
            costLabel.setTextFill(Color.LIGHTGRAY);
            costLabel.setFont(Font.font(12));
            costLabel.setMaxWidth(NODE_WIDTH - 10);
            costLabel.setTextAlignment(TextAlignment.CENTER);

            VBox textBox = new VBox(3);
            textBox.setAlignment(Pos.CENTER);
            if (isVisible) {
                textBox.getChildren().addAll(nameLabel, costLabel);
            } else {
                textBox.getChildren().add(nameLabel);
            }

            nodePane.getChildren().addAll(bg, textBox);

            if (isNodeVisible) {
                nodePane.setCursor(javafx.scene.Cursor.HAND);
                nodePane.setOnMouseEntered(e -> {
                    if (!isResearched && !isNodeAvailable) {
                        bg.setFill(Color.rgb(200, 50, 50));
                        bg.setStroke(Color.rgb(150, 30, 30));
                    } else {
                        highlightAncestors(currentNode, true);
                    }
                });
                nodePane.setOnMouseExited(e -> {
                    bg.setFill(nodeDefaultFill.get(node));
                    bg.setStroke(nodeDefaultStroke.get(node));
                    highlightAncestors(currentNode, false);
                });

                if (!isResearched && isNodeAvailable) {
                    nodePane.setOnMouseClicked(ev -> {
                        boolean success = false;
                        if (branchType.equals("tech")) success = controller.selectTech(currentNode.getName());
                        else if (branchType.equals("social")) success = controller.selectSocial(currentNode.getName());
                        else if (branchType.equals("religion")) success = controller.selectReligion(currentNode.getName());
                        if (success) {
                            overlayRoot.setVisible(false);
                            controller.updateStatus("Изучение '" + currentNode.getName() + "' начато.");
                        } else {
                            controller.updateStatus("Не удалось начать изучение.");
                        }
                    });
                } else {
                    nodePane.setOnMouseClicked(ev -> {
                        if (isResearched) controller.updateStatus("Технология уже изучена.");
                        else controller.updateStatus("Технология недоступна.");
                    });
                }
            }

            // Tooltip
            if (isVisible) {
                Tooltip tooltip = new Tooltip();
                tooltip.setStyle("-fx-font-size: 13px; -fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10;");
                StringBuilder ttText = new StringBuilder();
                ttText.append(node.getName()).append("\n");
                ttText.append("Стоимость: ").append(node.getCost()).append("\n");
                ttText.append("Требуется: ");
                if (node.getPrerequisites().isEmpty()) ttText.append("нет");
                else ttText.append(String.join(", ", node.getPrerequisites()));
                ttText.append("\n\n").append(node.getDescription());
                ttText.append("\n\nБонус: ").append(node.getBonusDescription());
                if (isResearched) ttText.append("\n\n✅ Изучено");
                else if (isNodeAvailable) ttText.append("\n\n🔓 Доступно");
                else ttText.append("\n\n🔒 Заблокировано");
                tooltip.setText(ttText.toString());
                Tooltip.install(nodePane, tooltip);
            } else {
                Tooltip tooltip = new Tooltip("Технология ещё не открыта");
                tooltip.setStyle("-fx-font-size: 13px; -fx-background-color: #333; -fx-text-fill: white;");
                Tooltip.install(nodePane, tooltip);
            }

            treePane.getChildren().add(nodePane);
            nodePanes.put(node, nodePane);
        }

        // --- Левая панель ---
        VBox leftPanel = new VBox(15);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setMaxWidth(280);
        leftPanel.setMinWidth(280);
        leftPanel.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10; -fx-padding: 10;");

        VBox diagramBox = createPoleDiagram();
        VBox statsBox = createStatsPanel();
        leftPanel.getChildren().addAll(diagramBox, statsBox);

        // --- Основной layout ---
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: rgba(26,26,46,0.95);");

        Label eraTitle = new Label("ДРЕВНИЙ МИР");
        eraTitle.setTextFill(Color.rgb(255, 215, 0));
        eraTitle.setFont(Font.font(32));
        eraTitle.setStyle("-fx-font-weight: bold; -fx-letter-spacing: 4px;");
        BorderPane.setAlignment(eraTitle, Pos.CENTER);
        BorderPane.setMargin(eraTitle, new Insets(15, 0, 10, 0));
        root.setTop(eraTitle);

        HBox centerBox = new HBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(0, 20, 20, 20));

        ScrollPane scrollPane = new ScrollPane(treePane);
        scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        centerBox.getChildren().addAll(leftPanel, scrollPane);
        root.setCenter(centerBox);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #6a4a4a; -fx-text-fill: white; -fx-padding: 5 15;");
        closeBtn.setOnAction(e -> overlayRoot.setVisible(false));
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(10));

        overlayRoot.getChildren().clear();
        overlayRoot.getChildren().addAll(root, closeBtn);
        StackPane.setAlignment(root, Pos.CENTER);
        overlayRoot.setVisible(true);
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =========================================================================

    private void determineVisibleNodes() {
        visibleNodes.clear();
        TechTree techTree = controller.getTechTree();
        for (TechNode node : nodes) {
            if (node.isResearched()) visibleNodes.add(node);
        }
        for (TechNode node : nodes) {
            if (node.getPrerequisites().isEmpty()) visibleNodes.add(node);
        }
        for (TechNode node : nodes) {
            if (visibleNodes.contains(node)) continue;
            for (String prereq : node.getPrerequisites()) {
                if (techTree.isResearched(prereq)) {
                    visibleNodes.add(node);
                    break;
                }
            }
        }
    }

    private void showLockedMessage() {
        VBox lockBox = new VBox(15);
        lockBox.setAlignment(Pos.CENTER);
        lockBox.setPadding(new Insets(20));
        lockBox.setStyle("-fx-background-color: rgba(30,30,40,0.95);");
        Label lockLabel = new Label("🔒 Религия заблокирована");
        lockLabel.setTextFill(Color.RED);
        lockLabel.setFont(Font.font(20));
        Label hint = new Label("Изучите 'Философию' в культуре, чтобы открыть религиозную ветку.");
        hint.setTextFill(Color.LIGHTGRAY);
        hint.setFont(Font.font(14));
        Button closeBtn = new Button("Закрыть");
        closeBtn.setStyle("-fx-font-size: 15px; -fx-background-color: #555; -fx-text-fill: white; -fx-padding: 8 20;");
        closeBtn.setOnAction(e -> overlayRoot.setVisible(false));
        lockBox.getChildren().addAll(lockLabel, hint, closeBtn);
        overlayRoot.getChildren().clear();
        overlayRoot.getChildren().add(lockBox);
        StackPane.setAlignment(lockBox, Pos.CENTER);
        overlayRoot.setVisible(true);
    }

    private VBox createStatsPanel() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-padding: 5;");

        Label statsTitle = new Label("ВЫРАБОТКА РЕСУРСОВ");
        statsTitle.setTextFill(Color.WHITE);
        statsTitle.setFont(Font.font(13));
        statsTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");

        int science = controller.getSciencePerTurn();
        int culture = controller.getCulturePerTurn();
        int faith = controller.getFaithPerTurn();

        Label scienceLabel = new Label("🔬 Наука: " + science + "/ход");
        scienceLabel.setTextFill(Color.CYAN);
        scienceLabel.setFont(Font.font(12));
        Label cultureLabel = new Label("🎭 Культура: " + culture + "/ход");
        cultureLabel.setTextFill(Color.MAGENTA);
        cultureLabel.setFont(Font.font(12));
        Label faithLabel = new Label("🙏 Вера: " + faith + "/ход");
        faithLabel.setTextFill(Color.LAVENDER);
        faithLabel.setFont(Font.font(12));

        Label graphTitle = new Label("Динамика за 10 ходов");
        graphTitle.setTextFill(Color.LIGHTGRAY);
        graphTitle.setFont(Font.font(11));
        graphTitle.setStyle("-fx-underline: true;");

        Canvas scienceCanvas = new Canvas(120, 50);
        Canvas cultureCanvas = new Canvas(120, 50);
        Canvas faithCanvas = new Canvas(120, 50);

        drawHistoryGraph(scienceCanvas, controller.getHistoryTracker().getScienceHistory(), Color.CYAN);
        drawHistoryGraph(cultureCanvas, controller.getHistoryTracker().getCultureHistory(), Color.MAGENTA);
        drawHistoryGraph(faithCanvas, controller.getHistoryTracker().getFaithHistory(), Color.LAVENDER);

        VBox graphsBox = new VBox(4);
        graphsBox.getChildren().addAll(
                new Label("Наука"), scienceCanvas,
                new Label("Культура"), cultureCanvas,
                new Label("Вера"), faithCanvas
        );
        graphsBox.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(statsTitle, scienceLabel, cultureLabel, faithLabel, graphTitle, graphsBox);
        return box;
    }

    private void drawHistoryGraph(Canvas canvas, List<Integer> history, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        gc.setFill(Color.rgb(30, 30, 40, 0.6));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.rgb(60, 60, 80));
        gc.setLineWidth(0.5);
        gc.strokeRect(0, 0, w, h);

        if (history.isEmpty()) {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font(10));
            gc.fillText("Нет данных", w / 2 - 30, h / 2 + 4);
            return;
        }

        int maxVal = history.stream().max(Integer::compareTo).orElse(1);
        if (maxVal == 0) maxVal = 1;
        double padding = 4;
        double plotW = w - 2 * padding;
        double plotH = h - 2 * padding;

        int points = history.size();
        double stepX = plotW / (points - 1);
        gc.setStroke(color);
        gc.setLineWidth(2);

        double[] xPoints = new double[points];
        double[] yPoints = new double[points];
        for (int i = 0; i < points; i++) {
            xPoints[i] = padding + i * stepX;
            double val = history.get(i);
            double norm = (double) val / maxVal;
            yPoints[i] = padding + plotH - norm * plotH;
        }

        gc.setFill(color.interpolate(Color.TRANSPARENT, 0.5));
        gc.beginPath();
        gc.moveTo(xPoints[0], padding + plotH);
        for (int i = 0; i < points; i++) {
            gc.lineTo(xPoints[i], yPoints[i]);
        }
        gc.lineTo(xPoints[points - 1], padding + plotH);
        gc.closePath();
        gc.fill();

        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.beginPath();
        gc.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < points; i++) {
            gc.lineTo(xPoints[i], yPoints[i]);
        }
        gc.stroke();

        gc.setFill(color);
        for (int i = 0; i < points; i++) {
            gc.fillOval(xPoints[i] - 2, yPoints[i] - 2, 4, 4);
        }

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font(8));
        gc.fillText("max:" + maxVal, padding, h - padding - 4);
        int minVal = history.stream().min(Integer::compareTo).orElse(0);
        gc.fillText("min:" + minVal, padding, 10);
    }

    private VBox createPoleDiagram() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_CENTER);

        Label diagramTitle = new Label("ПОЛЮСА ЦИВИЛИЗАЦИИ");
        diagramTitle.setTextFill(Color.WHITE);
        diagramTitle.setFont(Font.font(14));
        diagramTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");

        Canvas canvas = new Canvas(200, 200);
        drawSpiderChart(canvas);

        VBox legendBox = new VBox(4);
        legendBox.setStyle("-fx-padding: 5;");
        String[] axes = {
                "Наука/Вера", "Мир/Война", "Централизация",
                "Традиции/Реформы", "Элита/Народ", "Изоляция/Открытость",
                "Природа/Индустрия"
        };
        Color[] colors = {
                Color.CYAN, Color.RED, Color.YELLOW,
                Color.ORANGE, Color.PURPLE, Color.BLUE, Color.LIME
        };
        for (int i = 0; i < axes.length; i++) {
            HBox item = new HBox(5);
            Rectangle dot = new Rectangle(8, 8, colors[i]);
            Label lbl = new Label(axes[i]);
            lbl.setTextFill(Color.LIGHTGRAY);
            lbl.setFont(Font.font(10));
            item.getChildren().addAll(dot, lbl);
            legendBox.getChildren().add(item);
        }

        box.getChildren().addAll(diagramTitle, canvas, legendBox);
        return box;
    }

    private void drawSpiderChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = Math.min(width, height) * 0.4;
        int numAxes = 7;
        double[] values = controller.computePoleValues();

        gc.clearRect(0, 0, width, height);

        for (int i = 0; i < numAxes; i++) {
            double angle = Math.toRadians(90 - i * (360.0 / numAxes));
            double x = centerX + radius * Math.cos(angle);
            double y = centerY - radius * Math.sin(angle);
            gc.setStroke(Color.rgb(100, 100, 120, 0.5));
            gc.setLineWidth(1);
            gc.strokeLine(centerX, centerY, x, y);
        }

        for (int ring = 1; ring <= 3; ring++) {
            double r = radius * ring / 3.0;
            gc.setStroke(Color.rgb(100, 100, 120, 0.3));
            gc.strokeOval(centerX - r, centerY - r, 2 * r, 2 * r);
        }

        double[] xPoints = new double[numAxes];
        double[] yPoints = new double[numAxes];
        for (int i = 0; i < numAxes; i++) {
            double angle = Math.toRadians(90 - i * (360.0 / numAxes));
            double val = Math.max(0, Math.min(1, values[i] / 100.0));
            double r = radius * val;
            xPoints[i] = centerX + r * Math.cos(angle);
            yPoints[i] = centerY - r * Math.sin(angle);
        }
        gc.setFill(Color.rgb(100, 200, 255, 0.2));
        gc.fillPolygon(xPoints, yPoints, numAxes);
        gc.setStroke(Color.rgb(100, 200, 255, 0.8));
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, numAxes);

        for (int i = 0; i < numAxes; i++) {
            double angle = Math.toRadians(90 - i * (360.0 / numAxes));
            double x = centerX + (radius + 15) * Math.cos(angle);
            double y = centerY - (radius + 15) * Math.sin(angle);
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font(9));
            String label = String.valueOf((int) values[i]);
            gc.fillText(label, x - 10, y + 4);
        }
    }

    private void highlightAncestors(TechNode node, boolean highlight) {
        Set<TechNode> ancestors = new HashSet<>();
        collectAncestors(node, ancestors);

        for (TechNode n : nodes) {
            StackPane pane = nodePanes.get(n);
            if (pane == null) continue;
            Rectangle bg = (Rectangle) pane.getChildren().get(0);
            if (ancestors.contains(n) || n.equals(node)) {
                if (highlight) {
                    bg.setStroke(Color.YELLOW);
                    bg.setStrokeWidth(3);
                } else {
                    bg.setStroke(n.isResearched() ? Color.rgb(34, 120, 50) : Color.rgb(60, 60, 70));
                    bg.setStrokeWidth(2);
                }
            }
        }

        for (TechNode n : nodes) {
            List<Line> lines = incomingLines.get(n);
            if (lines == null) continue;
            boolean isAncestor = ancestors.contains(n) || n.equals(node);
            for (Line line : lines) {
                if (isAncestor) {
                    if (highlight) {
                        line.setStroke(Color.rgb(255, 255, 100, 0.9));
                        line.setStrokeWidth(3.5);
                    } else {
                        boolean allResearched = n.isResearched() && allPrereqsResearched(n);
                        line.setStroke(allResearched ? Color.rgb(100, 220, 100, 0.8) : Color.rgb(180, 180, 200, 0.4));
                        line.setStrokeWidth(2.5);
                    }
                }
            }
        }
    }

    private void collectAncestors(TechNode node, Set<TechNode> ancestors) {
        for (String prereqName : node.getPrerequisites()) {
            TechNode prereq = findNodeByName(prereqName);
            if (prereq != null) {
                ancestors.add(prereq);
                collectAncestors(prereq, ancestors);
            }
        }
    }

    private boolean allPrereqsResearched(TechNode node) {
        for (String prereq : node.getPrerequisites()) {
            TechNode p = findNodeByName(prereq);
            if (p == null || !p.isResearched()) return false;
        }
        return true;
    }

    private int calculateDepth(TechNode node, Set<String> visited) {
        if (visited.contains(node.getName())) return 0;
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

    private List<Line> drawOrthogonalLine(Pane pane, double x1, double y1, double x2, double y2, Color color) {
        List<Line> lines = new ArrayList<>();
        double midX = (x1 + x2) / 2;
        double arrowSize = 10;

        Line line1 = new Line(x1, y1, midX, y1);
        Line line2 = new Line(midX, y1, midX, y2);
        Line line3 = new Line(midX, y2, x2 - arrowSize, y2);

        line1.setStroke(color);
        line1.setStrokeWidth(2.5);
        line2.setStroke(color);
        line2.setStrokeWidth(2.5);
        line3.setStroke(color);
        line3.setStrokeWidth(2.5);

        pane.getChildren().addAll(line1, line2, line3);
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);

        double xTip = x2;
        double yTip = y2;
        double xBase1 = x2 - arrowSize;
        double yBase1 = y2 - arrowSize * 0.5;
        double xBase2 = x2 - arrowSize;
        double yBase2 = y2 + arrowSize * 0.5;

        Polygon arrow = new Polygon(xTip, yTip, xBase1, yBase1, xBase2, yBase2);
        arrow.setFill(color);
        pane.getChildren().add(arrow);

        return lines;
    }
}
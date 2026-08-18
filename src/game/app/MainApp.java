package game.app;

import engine.infrastructure.GameLoop;
import engine.infrastructure.GameState;
import game.controller.Advisor;
import game.controller.GameController;
import game.controller.InputHandler;
import game.UI.StaticMainMenu;
import game.UI.UIManager;
import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import game.model.world.Tile;
import game.model.world.World;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {

    private GameController controller;
    private UIManager uiManager;
    private Advisor advisor;
    private GameLoop gameLoop;
    private BorderPane canvasContainer;
    private InputHandler inputHandler;
    private Canvas canvas;

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas();
        canvas.setFocusTraversable(true);
        canvas.setMouseTransparent(false);

        // Создаём зависимости
        World world = new World();
        HexGrid hexGrid = new HexGrid(28);
        hexGrid.setPadding(200);
        GameState gameState = new GameState();
        TechTree techTree = new TechTree();

        // Создаём контроллер с зависимостями
        controller = new GameController(world, hexGrid, gameState, techTree);
        uiManager = new UIManager(controller);
        advisor = new Advisor(uiManager);
        controller.setAdvisor(advisor);
        controller.setUIManager(uiManager);

        // Колбэки
        controller.setOnUnitSelected(() -> {
            Platform.runLater(() -> {
                uiManager.refreshAll();
                uiManager.updateStatus("Юнит выбран.");
            });
        });
        controller.setOnCitySelected(() -> {
            Platform.runLater(() -> {
                uiManager.refreshAll();
                uiManager.updateStatus("Город выбран.");
            });
        });
        controller.setOnResourcesUpdated(() -> {
            Platform.runLater(() -> uiManager.updateResourcesUI());
        });
        controller.setOnProgressUpdated(() -> {
            Platform.runLater(() -> uiManager.updateProgressUI());
        });
        controller.setOnStatusChanged(() -> {
            Platform.runLater(() -> {
                uiManager.updateStatus("Ход завершён.");
                uiManager.updateTurn(controller.getTurnNumber());
            });
        });

        // UI
        StackPane root = new StackPane();

        StaticMainMenu.createMainMenu(this);
        uiManager.initUI(root, primaryStage);

        canvasContainer = new BorderPane();
        canvasContainer.setTop(uiManager.getTopPanel());
        canvasContainer.setCenter(canvas);
        canvasContainer.setVisible(false);

        root.getChildren().addAll(
                StaticMainMenu.mainMenuRoot,
                canvasContainer,
                uiManager.getProgressPanel(),
                uiManager.getResearchPanel(),
                uiManager.getUnitPanel(),
                uiManager.getCityPanel(),
                uiManager.getInfoPanel(),
                uiManager.getCityInfoPanel(),
                uiManager.getOverlayRoot(),
                uiManager.getCityNameInputOverlay()
        );

        StackPane.setAlignment(uiManager.getProgressPanel(), Pos.TOP_LEFT);
        StackPane.setMargin(uiManager.getProgressPanel(), new Insets(60, 0, 0, 10));
        StackPane.setAlignment(uiManager.getResearchPanel(), Pos.TOP_LEFT);
        StackPane.setMargin(uiManager.getResearchPanel(), new Insets(60, 0, 0, 10));
        StackPane.setAlignment(uiManager.getInfoPanel(), Pos.BOTTOM_LEFT);
        StackPane.setMargin(uiManager.getInfoPanel(), new Insets(0, 0, 20, 20));
        StackPane.setAlignment(uiManager.getUnitPanel(), Pos.BOTTOM_RIGHT);
        StackPane.setMargin(uiManager.getUnitPanel(), new Insets(0, 20, 20, 0));
        StackPane.setAlignment(uiManager.getCityPanel(), Pos.BOTTOM_RIGHT);
        StackPane.setMargin(uiManager.getCityPanel(), new Insets(0, 20, 20, 0));
        StackPane.setAlignment(uiManager.getCityInfoPanel(), Pos.TOP_LEFT);
        StackPane.setMargin(uiManager.getCityInfoPanel(), new Insets(60, 0, 0, 10));

        StaticMainMenu.mainMenuRoot.setVisible(true);
        canvasContainer.setVisible(false);
        uiManager.getProgressPanel().setVisible(false);
        uiManager.getResearchPanel().setVisible(false);
        uiManager.getUnitPanel().setVisible(false);
        uiManager.getCityPanel().setVisible(false);
        uiManager.getInfoPanel().setVisible(false);
        uiManager.getCityInfoPanel().setVisible(false);
        uiManager.getOverlayRoot().setVisible(false);
        uiManager.getCityNameInputOverlay().setVisible(false);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.setTitle("Глобальная стратегия");
        primaryStage.show();

        scene.setOnKeyPressed(event -> {
            if (!canvasContainer.isVisible()) return;
            KeyCode code = event.getCode();
            double step = 20;
            boolean moved = false;
            if (code == KeyCode.W) { controller.moveCamera(0, -step); moved = true; }
            else if (code == KeyCode.S) { controller.moveCamera(0, step); moved = true; }
            else if (code == KeyCode.A) { controller.moveCamera(-step, 0); moved = true; }
            else if (code == KeyCode.D) { controller.moveCamera(step, 0); moved = true; }
            if (moved) event.consume();
        });

        scene.widthProperty().addListener((obs, old, newVal) -> {
            if (canvasContainer.isVisible()) {
                controller.recalculateHexSize(canvas.getWidth(), canvas.getHeight());
            }
        });
        scene.heightProperty().addListener((obs, old, newVal) -> {
            if (canvasContainer.isVisible()) {
                controller.recalculateHexSize(canvas.getWidth(), canvas.getHeight());
            }
        });

        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty().subtract(uiManager.getTopPanel().heightProperty()));
    }

    public void startNewGame() {
        controller.startNewGame();

        StaticMainMenu.mainMenuRoot.setVisible(false);
        canvasContainer.setVisible(true);
        uiManager.getProgressPanel().setVisible(true);
        uiManager.getResearchPanel().setVisible(true);
        uiManager.getUnitPanel().setVisible(false);
        uiManager.getCityPanel().setVisible(false);
        uiManager.getInfoPanel().setVisible(false);
        uiManager.getCityInfoPanel().setVisible(false);
        uiManager.getOverlayRoot().setVisible(false);
        uiManager.getCityNameInputOverlay().setVisible(false);

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) { w = 800; h = 600; }
        controller.setCanvasSize(w, h);

        gameLoop = new GameLoop(controller.getWorld(), canvas, controller);
        gameLoop.setOverlayRenderer(this::renderCitiesOverlay);
        gameLoop.start();

        inputHandler = new InputHandler(controller, uiManager, canvas);

        uiManager.updateTurn(controller.getTurnNumber());
        uiManager.updateStatus("Новая игра начата.");
        uiManager.updateResourcesUI();
        uiManager.updateProgressUI();
        uiManager.updateResearchPanel();

        controller.updateWorldBoundsFromCanvas(w, h);
    }

    private void renderCitiesOverlay() {
        if (controller.isCityView()) return;
        if (controller.getCities().isEmpty()) return;
        var gc = canvas.getGraphicsContext2D();
        for (var city : controller.getCities()) {
            var center = city.getCenter();
            double[] screenPos = controller.getHexGrid().hexToScreen(center.col, center.row);
            double cx = screenPos[0];
            double cy = screenPos[1] - controller.getHexGrid().getHexSize() * 0.9;

            String name = city.getName();
            gc.setFill(Color.rgb(0, 0, 0, 0.75));
            gc.setFont(javafx.scene.text.Font.font(13));
            double textWidth = gc.getFont().getSize() * name.length() * 0.6;
            double rectW = textWidth + 12;
            double rectH = 22;
            double baseY = cy - rectH/2;
            gc.fillRoundRect(cx - rectW/2, baseY, rectW, rectH, 6, 6);
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font(13));
            gc.fillText(name, cx - textWidth/2, baseY + rectH - 4);

            String type = city.getType();
            String pop = "👤 " + city.getPopulation();
            String line2 = type + " | " + pop;
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.setFont(javafx.scene.text.Font.font(11));
            double line2Width = gc.getFont().getSize() * line2.length() * 0.55;
            double line2RectW = line2Width + 10;
            double line2RectH = 18;
            double line2Y = baseY + rectH + 2;
            gc.fillRoundRect(cx - line2RectW/2, line2Y, line2RectW, line2RectH, 4, 4);
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(javafx.scene.text.Font.font(11));
            gc.fillText(line2, cx - line2Width/2, line2Y + line2RectH - 4);

            int totalFood = 0, totalProd = 0;
            for (Hex hex : city.getTiles()) {
                Tile tile = controller.findGlobalTileAtHex(hex);
                if (tile != null) {
                    totalFood += tile.getTerrain().getFood();
                    totalProd += tile.getTerrain().getProduction();
                }
            }
            String project = "Нет";
            if (city.getProductionItem() != null) {
                if ("settler".equals(city.getProductionItem())) {
                    project = "Поселенец " + city.getProductionProgress() + "/" + city.getProductionTarget();
                } else {
                    project = city.getProductionItem();
                }
            }
            String line3 = "⚙ " + totalProd + " | " + project + " | 🍖 " + totalFood;
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.setFont(javafx.scene.text.Font.font(10));
            double line3Width = gc.getFont().getSize() * line3.length() * 0.55;
            double line3RectW = line3Width + 10;
            double line3RectH = 16;
            double line3Y = line2Y + line2RectH + 1;
            gc.fillRoundRect(cx - line3RectW/2, line3Y, line3RectW, line3RectH, 4, 4);
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.fillText(line3, cx - line3Width/2, line3Y + line3RectH - 3);

            int consumption = city.getPopulation() / 1000;
            int surplus = totalFood - consumption;
            String growth;
            if (surplus > 0) {
                growth = "📈 +" + surplus + " (профицит)";
            } else if (surplus < 0) {
                growth = "📉 " + surplus + " (дефицит)";
            } else {
                growth = "➡️ 0 (баланс)";
            }
            String line4 = "🍽️ " + growth;
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.setFont(javafx.scene.text.Font.font(10));
            double line4Width = gc.getFont().getSize() * line4.length() * 0.55;
            double line4RectW = line4Width + 10;
            double line4RectH = 16;
            double line4Y = line3Y + line3RectH + 1;
            gc.fillRoundRect(cx - line4RectW/2, line4Y, line4RectW, line4RectH, 4, 4);
            if (surplus > 0) {
                gc.setFill(Color.LIGHTGREEN);
            } else if (surplus < 0) {
                gc.setFill(Color.LIGHTSALMON);
            } else {
                gc.setFill(Color.LIGHTGRAY);
            }
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.fillText(line4, cx - line4Width/2, line4Y + line4RectH - 3);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
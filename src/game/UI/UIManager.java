package game.UI;

import game.controller.GameController;
import game.model.city.City;
import game.model.research.TechNode;
import game.model.unit.Unit;
import game.model.world.Hex;
import game.model.world.Tile;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class UIManager {

    private GameController controller;
    private StackPane root;

    private final ResearchPanelManager researchPanelManager;
    private final UIPanelManager uiPanelManager;
    private final TopPanelManager topPanelManager;

    private StackPane overlayRootLocal;
    private StackPane cityNameInputOverlayLocal;
    private TextField cityNameField;
    private Button cityNameOkButton, cityNameCancelButton;

    private Queue<AdvisorMessage> advisorQueue = new LinkedList<>();

    private static class AdvisorMessage {
        String title;
        String message;
        Runnable onClose;
        AdvisorMessage(String title, String message, Runnable onClose) {
            this.title = title;
            this.message = message;
            this.onClose = onClose;
        }
    }

    public UIManager(GameController controller) {
        this.controller = controller;

        // Создаём менеджеры панелей, передавая регистры из контроллера
        this.researchPanelManager = new ResearchPanelManager(controller, this);
        this.uiPanelManager = new UIPanelManager(
                controller,
                this,
                controller.getTechRegistry(),
                controller.getImprovementRegistry(),
                controller.getDistrictRegistry(),
                controller.getCenterImprovementRegistry()
        );
        this.topPanelManager = new TopPanelManager(controller, this);
    }

    public void initUI(StackPane root, Stage primaryStage) {
        this.root = root;
        overlayRootLocal = createOverlayRoot();
        cityNameInputOverlayLocal = createCityNameInputOverlay();
    }

    // Геттеры для панелей (делегирование)
    public HBox getTopPanel() { return topPanelManager.getTopPanel(); }
    public VBox getProgressPanel() { return uiPanelManager.getProgressPanel(); }
    public VBox getUnitPanel() { return uiPanelManager.getUnitPanel(); }
    public VBox getCityPanel() { return uiPanelManager.getCityPanel(); }
    public VBox getInfoPanel() { return uiPanelManager.getInfoPanel(); }
    public VBox getCityInfoPanel() { return uiPanelManager.getCityInfoPanel(); }
    public VBox getResearchPanel() { return researchPanelManager.getResearchPanel(); }
    public StackPane getOverlayRoot() { return overlayRootLocal; }
    public StackPane getCityNameInputOverlay() { return cityNameInputOverlayLocal; }

    public Label getStatusLabel() { return topPanelManager.getStatusLabel(); }
    public Label getTurnLabel() { return topPanelManager.getTurnLabel(); }
    public Button getEndTurnButton() { return topPanelManager.getEndTurnButton(); }
    public TextField getCityNameField() { return cityNameField; }

    // Обновления
    public void updateUnitPanel(Unit unit) { uiPanelManager.updateUnitPanel(unit); }
    public void updateCityPanel(City city) { uiPanelManager.updateCityPanel(city); }
    public void updateInfoPanel(Hex hex, Tile tile) { uiPanelManager.updateInfoPanel(hex, tile); }
    public void updateCityInfoPanel(City city) { uiPanelManager.updateCityInfoPanel(city); }
    public void updateResearchPanel() { researchPanelManager.updateResearchPanel(); }
    public void updateResourcesUI() { topPanelManager.updateResourcesUI(); }
    public void updateProgressUI() { /* пока пусто */ }
    public void updateStatus(String text) { topPanelManager.updateStatus(text); }
    public void updateTurn(int turn) { topPanelManager.updateTurn(turn); }

    public void refreshAll() {
        Unit unit = controller.getSelectedUnit();
        City city = controller.getSelectedCity();
        if (unit != null) updateUnitPanel(unit);
        else uiPanelManager.showUnitPanel(false);
        if (city != null) updateCityPanel(city);
        else uiPanelManager.showCityPanel(false);
        updateResourcesUI();
        updateResearchPanel();
        updateCityInfoPanel(city);
    }

    // Создание оверлеев
    private StackPane createOverlayRoot() {
        StackPane overlay = new StackPane();
        overlay.setVisible(false);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        VBox overlayContent = new VBox(15);
        overlayContent.setAlignment(Pos.CENTER);
        overlayContent.setStyle("-fx-background-color: rgba(30,30,40,0.95); -fx-padding: 20; -fx-border-radius: 10;");
        overlayContent.setMaxWidth(800);
        overlayContent.setMaxHeight(600);
        overlay.getChildren().add(overlayContent);
        StackPane.setAlignment(overlayContent, Pos.CENTER);
        return overlay;
    }

    private StackPane createCityNameInputOverlay() {
        StackPane overlay = new StackPane();
        overlay.setVisible(false);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        VBox inputBox = new VBox(15);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setStyle("-fx-background-color: rgba(30,30,40,0.95); -fx-padding: 20; -fx-border-radius: 10;");
        inputBox.setMaxWidth(300);
        Label inputLabel = new Label("Введите название города:");
        inputLabel.setTextFill(Color.WHITE);
        inputLabel.setFont(Font.font(16));
        cityNameField = new TextField("Новый город");
        cityNameField.setStyle("-fx-font-size: 14px;");
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        cityNameOkButton = new Button("ОК");
        cityNameOkButton.setStyle("-fx-font-size: 14px; -fx-background-color: #2a7a2a; -fx-text-fill: white;");
        cityNameOkButton.setOnAction(e -> {
            String name = cityNameField.getText().trim();
            if (name.isEmpty()) name = "Город_" + (controller.getCities().size() + 1);
            controller.foundCity(controller.getSelectedUnit(), name);
            overlay.setVisible(false);
        });
        cityNameCancelButton = new Button("Отмена");
        cityNameCancelButton.setStyle("-fx-font-size: 14px; -fx-background-color: #555; -fx-text-fill: white;");
        cityNameCancelButton.setOnAction(e -> overlay.setVisible(false));
        buttonsBox.getChildren().addAll(cityNameOkButton, cityNameCancelButton);
        inputBox.getChildren().addAll(inputLabel, cityNameField, buttonsBox);
        overlay.getChildren().add(inputBox);
        return overlay;
    }

    // Сообщения советника
    public void showAdvisorMessage(String title, String message, Runnable onClose) {
        advisorQueue.add(new AdvisorMessage(title, message, onClose));
        showNextAdvisorMessage();
    }

    private void showNextAdvisorMessage() {
        if (advisorQueue.isEmpty() || overlayRootLocal.isVisible()) return;
        AdvisorMessage msg = advisorQueue.poll();
        if (msg == null) return;
        overlayRootLocal.setVisible(true);
        overlayRootLocal.getChildren().clear();

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: rgba(30,30,40,0.95); -fx-padding: 20; -fx-border-radius: 10;");
        content.setMaxWidth(500);
        content.setMaxHeight(400);

        Label titleLabel = new Label(msg.title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font(18));
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label messageLabel = new Label(msg.message);
        messageLabel.setTextFill(Color.LIGHTGRAY);
        messageLabel.setFont(Font.font(14));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(460);

        Button okButton = new Button("Понятно");
        okButton.setStyle("-fx-font-size: 14px; -fx-background-color: #2a7a2a; -fx-text-fill: white; -fx-padding: 8 20;");
        okButton.setOnAction(e -> {
            overlayRootLocal.setVisible(false);
            overlayRootLocal.getChildren().clear();
            if (msg.onClose != null) msg.onClose.run();
            showNextAdvisorMessage();
        });

        content.getChildren().addAll(titleLabel, messageLabel, okButton);
        overlayRootLocal.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER);
    }

    // Показать дерево технологий
    public void showTechTreeOverlay(String type) {
        List<TechNode> nodes;
        String title;
        if (type.equals("tech")) {
            nodes = controller.getTechTree().getTechs();
            title = "🔬 Древо технологий";
        } else if (type.equals("social")) {
            nodes = controller.getTechTree().getSocials();
            title = "🎭 Древо культуры";
        } else {
            nodes = controller.getTechTree().getReligions();
            title = "🙏 Древо религии";
        }
        TechTreeInteractiveOverlay overlay = new TechTreeInteractiveOverlay(nodes, title, controller, overlayRootLocal, type);
        overlay.show();
    }

    public static class ProductionButtonItem {
        public String text;
        public Runnable action;
        public String tooltipText;
        public boolean available;

        public ProductionButtonItem(String text, Runnable action, String tooltipText, boolean available) {
            this.text = text;
            this.action = action;
            this.tooltipText = tooltipText;
            this.available = available;
        }
    }
}
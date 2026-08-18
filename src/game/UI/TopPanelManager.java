package game.UI;

import game.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class TopPanelManager {

    private final GameController controller;
    private final UIManager uiManager;

    private HBox topPanel;
    private Label turnLabel;
    private Label statusLabel;
    private Button endTurnButton;
    private Label scienceLabel, cultureLabel, treasuryLabel, pietyLabel, legitimacyLabel;
    private ProgressBar legitimacyProgressBar;
    private HBox legitimacyBox;
    private Button govButton;

    public TopPanelManager(GameController controller, UIManager uiManager) {
        this.controller = controller;
        this.uiManager = uiManager;
        this.topPanel = createTopPanel();
    }

    public HBox getTopPanel() { return topPanel; }
    public Label getTurnLabel() { return turnLabel; }
    public Label getStatusLabel() { return statusLabel; }
    public Button getEndTurnButton() { return endTurnButton; }

    public void updateTurn(int turn) {
        turnLabel.setText("Ход: " + turn);
    }

    public void updateStatus(String text) {
        statusLabel.setText(text);
    }

    public void updateResourcesUI() {
        scienceLabel.setText("🔬 " + controller.getSciencePerTurn() + "/ход");
        cultureLabel.setText("🎭 " + controller.getCulturePerTurn() + "/ход");
        if (controller.isMoneyUnlocked()) {
            treasuryLabel.setText("💰 " + controller.getTreasury());
            treasuryLabel.setVisible(true);
        } else {
            treasuryLabel.setVisible(false);
        }
        if (controller.isReligionUnlocked()) {
            pietyLabel.setText("🙏 " + controller.getPiety());
            pietyLabel.setVisible(true);
        } else {
            pietyLabel.setVisible(false);
        }

        boolean legitUnlocked = controller.isLegitimacyUnlocked();
        int legit = controller.getGameState().getLegitimacy();
        if (legitUnlocked) {
            legitimacyLabel.setText("👑 " + legit + "/100");
            legitimacyLabel.setVisible(true);
            legitimacyProgressBar.setProgress(legit / 100.0);
            if (legit < 30) {
                legitimacyProgressBar.setStyle("-fx-accent: #e74c3c;");
            } else if (legit < 70) {
                legitimacyProgressBar.setStyle("-fx-accent: #f1c40f;");
            } else {
                legitimacyProgressBar.setStyle("-fx-accent: #2ecc71;");
            }
            legitimacyProgressBar.setVisible(true);
        } else {
            legitimacyLabel.setText("🔒 👑 --/100");
            legitimacyLabel.setVisible(true);
            legitimacyProgressBar.setVisible(false);
        }

        // Кнопка правительства всегда видна
        govButton.setVisible(true);
    }

    private HBox createTopPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(8, 15, 8, 15));
        panel.setStyle("-fx-background-color: #333333;");
        panel.setAlignment(Pos.CENTER_LEFT);

        turnLabel = new Label("Ход: 1");
        turnLabel.setTextFill(Color.WHITE);
        turnLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        statusLabel = new Label("WASD – камера | ПКМ – панорамирование | Колёсико – зум");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setStyle("-fx-font-size: 14px;");

        scienceLabel = new Label("🔬 0/ход");
        scienceLabel.setTextFill(Color.CYAN);
        scienceLabel.setStyle("-fx-font-size: 14px;");
        cultureLabel = new Label("🎭 0/ход");
        cultureLabel.setTextFill(Color.MAGENTA);
        cultureLabel.setStyle("-fx-font-size: 14px;");
        treasuryLabel = new Label("💰 0");
        treasuryLabel.setTextFill(Color.GOLD);
        treasuryLabel.setStyle("-fx-font-size: 14px;");
        pietyLabel = new Label("🙏 0");
        pietyLabel.setTextFill(Color.LAVENDER);
        pietyLabel.setStyle("-fx-font-size: 14px;");

        legitimacyLabel = new Label("👑 50/100");
        legitimacyLabel.setTextFill(Color.rgb(255, 215, 0));
        legitimacyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        legitimacyProgressBar = new ProgressBar(0.5);
        legitimacyProgressBar.setPrefWidth(80);
        legitimacyProgressBar.setMaxHeight(12);
        legitimacyProgressBar.setStyle("-fx-accent: #2ecc71;");

        legitimacyBox = new HBox(5);
        legitimacyBox.setAlignment(Pos.CENTER_LEFT);
        legitimacyBox.getChildren().addAll(legitimacyLabel, legitimacyProgressBar);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        endTurnButton = new Button("Завершить ход");
        endTurnButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        endTurnButton.setOnAction(e -> controller.endTurn());

        // Кнопка правительства всегда видна, но клик проверяет наличие "Вождества"
        govButton = new Button("⚖️ Правительство");
        govButton.setStyle("-fx-font-size: 12px; -fx-background-color: #4a5a7a; -fx-text-fill: white;");
        govButton.setVisible(true);
        govButton.setOnAction(e -> {
            if (controller.isLegitimacyUnlocked()) {
                uiManager.showGovernmentOverlay();
            } else {
                controller.updateStatus("Панель правительства станет доступна после изучения 'Вождество'.");
            }
        });

        panel.getChildren().addAll(
                turnLabel, statusLabel, spacer,
                scienceLabel, cultureLabel, treasuryLabel, pietyLabel,
                legitimacyBox,
                govButton,
                endTurnButton
        );

        treasuryLabel.setVisible(false);
        pietyLabel.setVisible(false);

        return panel;
    }
}
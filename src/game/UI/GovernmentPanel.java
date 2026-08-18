package game.UI;

import game.controller.GameController;
import game.controller.GovernmentManager;
import game.model.government.Law;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Map;

public class GovernmentPanel {

    private final GameController controller;
    private final GovernmentManager govManager;
    private final VBox panel;

    // Левая часть (списки)
    private final VBox leftBox;
    private final Label govLabel;
    private final ProgressBar legitimacyBar;
    private final Label legitimacyValueLabel;
    private final ListView<String> factorListView;
    private final ListView<String> activeLawsListView;
    private final ListView<String> availableLawsListView;
    private final ListView<String> unavailableLawsListView;
    private final Button adoptButton;
    private final Button repealButton;

    // Правая часть (политический процесс)
    private final VBox rightBox;
    private final Label processTitle;
    private final Label processLawName;
    private final Label processTurns;
    private final Label processChance;
    private final Canvas processCanvas;

    public GovernmentPanel(GameController controller) {
        this.controller = controller;
        this.govManager = controller.getGovernmentManager();

        // Левая часть
        this.leftBox = new VBox(6);
        this.leftBox.setAlignment(Pos.TOP_LEFT);
        this.leftBox.setPadding(new Insets(5));

        // Правая часть
        this.rightBox = new VBox(10);
        this.rightBox.setAlignment(Pos.CENTER);
        this.rightBox.setPadding(new Insets(10));
        this.rightBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 8;");
        this.rightBox.setPrefWidth(200);

        // Инициализация компонентов
        govLabel = new Label("Форма правления: Вождество");
        govLabel.setTextFill(Color.LIGHTYELLOW);
        govLabel.setStyle("-fx-font-size: 16px;");

        // Легитимность
        HBox legitimacyBox = new HBox(10);
        legitimacyBox.setAlignment(Pos.CENTER_LEFT);
        Label legLabel = new Label("Легитимность:");
        legLabel.setTextFill(Color.WHITE);
        legLabel.setStyle("-fx-font-size: 13px;");
        legitimacyBar = new ProgressBar(0.5);
        legitimacyBar.setPrefWidth(200);
        legitimacyBar.setMaxHeight(16);
        legitimacyValueLabel = new Label("50/100");
        legitimacyValueLabel.setTextFill(Color.WHITE);
        legitimacyValueLabel.setStyle("-fx-font-size: 13px;");
        legitimacyBox.getChildren().addAll(legLabel, legitimacyBar, legitimacyValueLabel);

        // Факторы (компактный список)
        factorListView = new ListView<>();
        factorListView.setPrefHeight(80);
        factorListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 11px;");

        // Списки законов (уменьшенная высота)
        activeLawsListView = new ListView<>();
        activeLawsListView.setPrefHeight(80);
        activeLawsListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 11px;");

        availableLawsListView = new ListView<>();
        availableLawsListView.setPrefHeight(80);
        availableLawsListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 11px;");

        unavailableLawsListView = new ListView<>();
        unavailableLawsListView.setPrefHeight(80);
        unavailableLawsListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 11px;");

        // Кнопки (без кнопки "Закрыть")
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        adoptButton = new Button("✅ Принять");
        adoptButton.setStyle("-fx-font-size: 11px; -fx-background-color: #2a7a2a; -fx-text-fill: white;");
        adoptButton.setOnAction(e -> adoptSelectedLaw());
        repealButton = new Button("❌ Отменить");
        repealButton.setStyle("-fx-font-size: 11px; -fx-background-color: #8b4500; -fx-text-fill: white;");
        repealButton.setOnAction(e -> repealSelectedLaw());
        buttonBox.getChildren().addAll(adoptButton, repealButton);

        // Собираем левую часть
        leftBox.getChildren().addAll(
                govLabel,
                legitimacyBox,
                new Label("Факторы:"),
                factorListView,
                new Label("Принятые:"),
                activeLawsListView,
                new Label("Доступные:"),
                availableLawsListView,
                new Label("Недоступные:"),
                unavailableLawsListView,
                buttonBox
        );

        // Правая часть – политический процесс
        processTitle = new Label("⚖️ ПРОЦЕСС");
        processTitle.setTextFill(Color.WHITE);
        processTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        processLawName = new Label("—");
        processLawName.setTextFill(Color.LIGHTYELLOW);
        processLawName.setStyle("-fx-font-size: 12px;");

        processTurns = new Label("—");
        processTurns.setTextFill(Color.LIGHTGRAY);
        processTurns.setStyle("-fx-font-size: 12px;");

        processChance = new Label("—");
        processChance.setTextFill(Color.LIGHTCYAN);
        processChance.setStyle("-fx-font-size: 12px;");

        processCanvas = new Canvas(80, 80);

        rightBox.getChildren().addAll(
                processTitle,
                processLawName,
                processCanvas,
                processTurns,
                processChance
        );
        rightBox.setVisible(false);

        // Основная панель (HBox)
        HBox mainBox = new HBox(20);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(15));
        mainBox.getChildren().addAll(leftBox, rightBox);

        // Заголовок сверху
        Label title = new Label("⚖️ ПРАВИТЕЛЬСТВО");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);
        topBox.getChildren().add(title);

        // Финал
        panel = new VBox(10);
        panel.setStyle("-fx-background-color: rgba(20,20,30,0.95); -fx-padding: 15; -fx-background-radius: 10;");
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMaxHeight(Double.MAX_VALUE);
        panel.setPrefWidth(900);
        panel.setPrefHeight(600);
        panel.getChildren().addAll(topBox, mainBox);
    }

    // ========================================================================
    // Вспомогательные методы
    // ========================================================================

    private void adoptSelectedLaw() {
        String selected = availableLawsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        for (Law law : govManager.getAvailableLaws()) {
            String display = law.getName() + " (" + getBonusString(law) + ")";
            if (display.equals(selected)) {
                if (govManager.startPoliticalProcess(law)) {
                    updatePanel();
                    controller.updateUI();
                }
                break;
            }
        }
    }

    private void repealSelectedLaw() {
        String selected = activeLawsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        for (Law law : govManager.getActiveLaws()) {
            String display = "✅ " + law.getName() + " (" + getBonusString(law) + ")";
            if (display.equals(selected)) {
                if (govManager.repealLaw(law)) {
                    controller.updateStatus("Отменён закон: " + law.getName());
                    updatePanel();
                    controller.updateUI();
                }
                break;
            }
        }
    }

    private String getBonusString(Law law) {
        StringBuilder sb = new StringBuilder();
        if (law.getScienceBonus() != 0) sb.append("наука ").append(law.getScienceBonus() > 0 ? "+" : "").append(law.getScienceBonus()).append(" ");
        if (law.getCultureBonus() != 0) sb.append("культура ").append(law.getCultureBonus() > 0 ? "+" : "").append(law.getCultureBonus()).append(" ");
        if (law.getProductionBonus() != 0) sb.append("произв. ").append(law.getProductionBonus() > 0 ? "+" : "").append(law.getProductionBonus()).append(" ");
        if (law.getHappinessBonus() != 0) sb.append("счастье ").append(law.getHappinessBonus() > 0 ? "+" : "").append(law.getHappinessBonus()).append(" ");
        if (law.getLegitimacyBonus() != 0) sb.append("легит. ").append(law.getLegitimacyBonus() > 0 ? "+" : "").append(law.getLegitimacyBonus()).append(" ");
        if (law.getFaithBonus() != 0) sb.append("вера ").append(law.getFaithBonus() > 0 ? "+" : "").append(law.getFaithBonus()).append(" ");
        return sb.toString().trim();
    }

    public void updatePanel() {
        // Форма правления
        String gov = govManager.getCurrentGovernment();
        govLabel.setText("Форма правления: " + gov);

        // Легитимность (исправлена опечатка)
        int legitimacy = controller.getGameState().getLegitimacy();
        double progress = legitimacy / 100.0;
        legitimacyBar.setProgress(progress);
        legitimacyValueLabel.setText(legitimacy + "/100");
        String color;
        if (legitimacy < 30) color = "#e74c3c";
        else if (legitimacy < 70) color = "#f1c40f";
        else color = "#2ecc71";
        legitimacyBar.setStyle("-fx-accent: " + color + ";");

        // Факторы
        List<String> factors = controller.getLegitimacyFactors();
        factorListView.getItems().clear();
        if (factors.isEmpty()) {
            factorListView.getItems().add("Нет данных");
        } else {
            factorListView.getItems().addAll(factors);
        }
        Map<String, Integer> mods = govManager.getLegitimacyModifiers();
        if (!mods.isEmpty()) {
            factorListView.getItems().add("--- СОБЫТИЯ ---");
            for (Map.Entry<String, Integer> entry : mods.entrySet()) {
                String sign = entry.getValue() > 0 ? "+" : "";
                factorListView.getItems().add(sign + entry.getValue() + " " + entry.getKey());
            }
        }

        // Активные законы
        List<Law> active = govManager.getActiveLaws();
        activeLawsListView.getItems().clear();
        if (active.isEmpty()) {
            activeLawsListView.getItems().add("Нет принятых законов");
        } else {
            for (Law law : active) {
                activeLawsListView.getItems().add("✅ " + law.getName() + " (" + getBonusString(law) + ")");
            }
        }

        // Доступные законы
        List<Law> available = govManager.getAvailableLaws();
        availableLawsListView.getItems().clear();
        if (available.isEmpty()) {
            if (govManager.isPoliticalProcessActive()) {
                availableLawsListView.getItems().add("⏳ Процесс идёт...");
            } else {
                availableLawsListView.getItems().add("Нет доступных законов");
            }
        } else {
            for (Law law : available) {
                availableLawsListView.getItems().add(law.getName() + " (" + getBonusString(law) + ")");
            }
        }

        // Временно недоступные
        List<Law> unavailable = govManager.getTemporarilyUnavailableLaws();
        unavailableLawsListView.getItems().clear();
        if (unavailable.isEmpty()) {
            unavailableLawsListView.getItems().add("Нет временно недоступных законов");
        } else {
            for (Law law : unavailable) {
                String reason = "";
                if (govManager.getRepealCooldown(law) > 0) {
                    reason = " (перезарядка " + govManager.getRepealCooldown(law) + " ходов)";
                } else if (law.getRequiredTech() != null && !controller.getTechTree().isResearched(law.getRequiredTech())) {
                    reason = " (требуется технология: " + law.getRequiredTech() + ")";
                } else if (law.getRequiredGovernment() != null && !law.getRequiredGovernment().equals(gov)) {
                    reason = " (требуется правление: " + law.getRequiredGovernment() + ")";
                }
                unavailableLawsListView.getItems().add("🔒 " + law.getName() + reason);
            }
        }

        // Политический процесс
        if (govManager.isPoliticalProcessActive()) {
            rightBox.setVisible(true);
            Law pending = govManager.getPendingLaw();
            int current = govManager.getPoliticalProcessTurns();
            int max = govManager.getPoliticalProcessMaxTurns();
            double procProgress = Math.min(1.0, (double) current / max);

            processLawName.setText(pending.getName());
            processTurns.setText("Ходов: " + (max - current) + " / " + max);

            int leg = controller.getGameState().getLegitimacy();
            double chance = 0.4 + (leg / 100.0) * 0.4;
            switch (govManager.getCurrentGovernment()) {
                case "Империя": chance = Math.min(0.95, chance + 0.15); break;
                case "Монархия": chance = Math.min(0.90, chance + 0.10); break;
                case "Республика": chance = Math.min(0.85, chance + 0.05); break;
                case "Олигархия": chance = Math.min(0.85, chance + 0.05); break;
                default: break;
            }
            processChance.setText("Шанс: " + (int)(chance * 100) + "%");
            drawProgressCircle(processCanvas, procProgress);
        } else {
            rightBox.setVisible(false);
        }
    }

    private void drawProgressCircle(Canvas canvas, double progress) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double size = canvas.getWidth();
        double centerX = size/2;
        double centerY = size/2;
        double radius = 30;

        gc.clearRect(0, 0, size, size);
        gc.setFill(Color.rgb(40, 40, 50));
        gc.fillOval(0, 0, size, size);

        gc.setStroke(Color.rgb(60, 60, 70));
        gc.setLineWidth(5);
        gc.strokeArc(centerX - radius, centerY - radius, radius*2, radius*2, 0, 360, ArcType.OPEN);

        if (progress > 0) {
            gc.setStroke(Color.rgb(46, 204, 113));
            gc.setLineWidth(5);
            double angle = progress * 360;
            gc.strokeArc(centerX - radius, centerY - radius, radius*2, radius*2, 90, -angle, ArcType.OPEN);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(14));
        gc.fillText((int)(progress * 100) + "%", centerX - 20, centerY + 6);
    }

    public VBox getPanel() { return panel; }

    public void show() {
        if (!controller.isGovernmentUnlocked()) {
            controller.updateStatus("Панель правительства станет доступна после изучения 'Вождество'.");
            return;
        }
        if (controller.getAdvisor() != null) {
            controller.getAdvisor().showGovernmentPanelTutorial();
        }
        updatePanel();
        panel.setVisible(true);
    }
}
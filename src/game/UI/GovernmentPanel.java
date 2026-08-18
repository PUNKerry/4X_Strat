package game.UI;

import game.controller.GameController;
import game.controller.GovernmentManager;
import game.model.government.Law;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

public class GovernmentPanel {

    private final GameController controller;
    private final GovernmentManager govManager;
    private final VBox panel;
    private  Label govLabel;
    private  ProgressBar legitimacyBar;
    private  Label legitimacyValueLabel;
    private  ListView<String> factorListView;
    private  ListView<String> activeLawsListView;
    private  ListView<String> availableLawsListView;
    private  ListView<String> unavailableLawsListView;
    private  Button adoptButton;
    private  Button repealButton;

    public GovernmentPanel(GameController controller) {
        this.controller = controller;
        this.govManager = controller.getGovernmentManager();
        this.panel = createPanel();
    }

    private VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: rgba(20,20,30,0.95); -fx-padding: 20; -fx-background-radius: 10;");
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMaxHeight(Double.MAX_VALUE);
        panel.setPrefWidth(800);
        panel.setPrefHeight(600);

        Label title = new Label("⚖️ ПРАВИТЕЛЬСТВО");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        govLabel = new Label("Форма правления: Вождество");
        govLabel.setTextFill(Color.LIGHTYELLOW);
        govLabel.setStyle("-fx-font-size: 18px;");

        // Легитимность
        HBox legitimacyBox = new HBox(10);
        legitimacyBox.setAlignment(Pos.CENTER_LEFT);

        Label legLabel = new Label("Легитимность:");
        legLabel.setTextFill(Color.WHITE);
        legLabel.setStyle("-fx-font-size: 14px;");

        legitimacyBar = new ProgressBar(0.5);
        legitimacyBar.setPrefWidth(300);
        legitimacyBar.setMaxHeight(20);
        legitimacyBar.setStyle("-fx-accent: #2ecc71;");

        legitimacyValueLabel = new Label("50/100");
        legitimacyValueLabel.setTextFill(Color.WHITE);
        legitimacyValueLabel.setStyle("-fx-font-size: 14px;");

        legitimacyBox.getChildren().addAll(legLabel, legitimacyBar, legitimacyValueLabel);

        // Факторы легитимности
        Label factorsTitle = new Label("Факторы, влияющие на легитимность:");
        factorsTitle.setTextFill(Color.WHITE);
        factorsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        factorListView = new ListView<>();
        factorListView.setPrefHeight(100);
        factorListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 12px;");

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        // Активные законы
        Label activeTitle = new Label("Принятые законы:");
        activeTitle.setTextFill(Color.WHITE);
        activeTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        activeLawsListView = new ListView<>();
        activeLawsListView.setPrefHeight(120);
        activeLawsListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 12px;");

        // Доступные законы
        Label availableTitle = new Label("Доступные законы:");
        availableTitle.setTextFill(Color.WHITE);
        availableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        availableLawsListView = new ListView<>();
        availableLawsListView.setPrefHeight(120);
        availableLawsListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 12px;");

        // Временно недоступные законы
        Label unavailableTitle = new Label("Временно недоступные законы:");
        unavailableTitle.setTextFill(Color.WHITE);
        unavailableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        unavailableLawsListView = new ListView<>();
        unavailableLawsListView.setPrefHeight(120);
        unavailableLawsListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 12px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        adoptButton = new Button("✅ Принять закон");
        adoptButton.setStyle("-fx-font-size: 12px; -fx-background-color: #2a7a2a; -fx-text-fill: white;");
        adoptButton.setOnAction(e -> adoptSelectedLaw());

        repealButton = new Button("❌ Отменить закон");
        repealButton.setStyle("-fx-font-size: 12px; -fx-background-color: #8b4500; -fx-text-fill: white;");
        repealButton.setOnAction(e -> repealSelectedLaw());

        Button closeButton = new Button("✕ Закрыть");
        closeButton.setStyle("-fx-font-size: 14px; -fx-background-color: #555; -fx-text-fill: white;");
        closeButton.setOnAction(e -> panel.setVisible(false));

        buttonBox.getChildren().addAll(adoptButton, repealButton, closeButton);

        panel.getChildren().addAll(
                title, govLabel,
                legitimacyBox,
                factorsTitle, factorListView,
                sep1,
                activeTitle, activeLawsListView,
                availableTitle, availableLawsListView,
                unavailableTitle, unavailableLawsListView,
                sep2,
                buttonBox
        );

        return panel;
    }

    private void adoptSelectedLaw() {
        String selected = availableLawsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        for (Law law : govManager.getAvailableLaws()) {
            String display = law.getName() + " (" + getBonusString(law) + ")";
            if (display.equals(selected)) {
                if (govManager.adoptLaw(law)) {
                    controller.updateStatus("Принят закон: " + law.getName());
                    updatePanel();
                    controller.updateUI();
                } else {
                    controller.updateStatus("Не удалось принять закон.");
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
                } else {
                    controller.updateStatus("Не удалось отменить закон.");
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

        // Легитимность
        int legitimacy = controller.getGameState().getLegitimacy();
        double progress = legitimacy / 100.0;
        legitimacyBar.setProgress(progress);
        legitimacyValueLabel.setText(legitimacy + "/100");

        // Цвет бара
        String color;
        if (legitimacy < 30) color = "#e74c3c";
        else if (legitimacy < 70) color = "#f1c40f";
        else color = "#2ecc71";
        legitimacyBar.setStyle("-fx-accent: " + color + ";");

        // Факторы легитимности
        List<String> factors = controller.getLegitimacyFactors();
        factorListView.getItems().clear();
        if (factors.isEmpty()) {
            factorListView.getItems().add("Нет данных");
        } else {
            factorListView.getItems().addAll(factors);
        }

        // Списки законов
        // Активные
        List<Law> active = govManager.getActiveLaws();
        activeLawsListView.getItems().clear();
        if (active.isEmpty()) {
            activeLawsListView.getItems().add("Нет принятых законов");
        } else {
            for (Law law : active) {
                activeLawsListView.getItems().add("✅ " + law.getName() + " (" + getBonusString(law) + ")");
            }
        }

        // Доступные
        List<Law> available = govManager.getAvailableLaws();
        availableLawsListView.getItems().clear();
        if (available.isEmpty()) {
            availableLawsListView.getItems().add("Нет доступных законов");
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
    }

    public VBox getPanel() {
        return panel;
    }

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
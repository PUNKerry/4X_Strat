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
    private Label govLabel;
    private Label statsLabel;
    private ListView<String> lawListView;
    private Button adoptButton;
    private Button repealButton;

    public GovernmentPanel(GameController controller) {
        this.controller = controller;
        this.govManager = controller.getGovernmentManager();
        this.panel = createPanel();
    }

    private VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: rgba(30,30,50,0.95); -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #888; -fx-border-width: 2; -fx-border-radius: 10;");
        panel.setMaxWidth(450);
        panel.setPrefWidth(450);

        Label title = new Label("⚖️ ПРАВИТЕЛЬСТВО");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        govLabel = new Label("Форма правления: Родоплеменной строй");
        govLabel.setTextFill(Color.LIGHTYELLOW);
        govLabel.setStyle("-fx-font-size: 14px;");

        statsLabel = new Label("Бонусы: наука +0, культура +0, производство +0, счастье +0, легитимность +0, вера +0");
        statsLabel.setTextFill(Color.LIGHTGRAY);
        statsLabel.setStyle("-fx-font-size: 12px;");
        statsLabel.setWrapText(true);

        Separator sep1 = new Separator();

        Label lawsTitle = new Label("Доступные законы:");
        lawsTitle.setTextFill(Color.WHITE);
        lawsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        lawListView = new ListView<>();
        lawListView.setPrefHeight(200);
        lawListView.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-font-size: 12px;");

        Separator sep2 = new Separator();

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        adoptButton = new Button("✅ Принять закон");
        adoptButton.setStyle("-fx-font-size: 12px; -fx-background-color: #2a7a2a; -fx-text-fill: white;");
        adoptButton.setOnAction(e -> adoptSelectedLaw());

        repealButton = new Button("❌ Отменить закон");
        repealButton.setStyle("-fx-font-size: 12px; -fx-background-color: #8b4500; -fx-text-fill: white;");
        repealButton.setOnAction(e -> repealSelectedLaw());

        buttonBox.getChildren().addAll(adoptButton, repealButton);

        panel.getChildren().addAll(title, govLabel, statsLabel, sep1, lawsTitle, lawListView, sep2, buttonBox);
        return panel;
    }

    private void adoptSelectedLaw() {
        String selected = lawListView.getSelectionModel().getSelectedItem();
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
        String selected = lawListView.getSelectionModel().getSelectedItem();
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

        // Бонусы
        int science = govManager.getTotalScienceBonus();
        int culture = govManager.getTotalCultureBonus();
        int production = govManager.getTotalProductionBonus();
        int happiness = govManager.getTotalHappinessBonus();
        int legitimacy = govManager.getTotalLegitimacyBonus();
        int faith = govManager.getTotalFaithBonus();

        statsLabel.setText(String.format(
                "Бонусы: наука %+d, культура %+d, производство %+d, счастье %+d, легитимность %+d, вера %+d",
                science, culture, production, happiness, legitimacy, faith
        ));

        // Списки законов
        List<Law> active = govManager.getActiveLaws();
        List<Law> available = govManager.getAvailableLaws();

        lawListView.getItems().clear();

        if (!active.isEmpty()) {
            lawListView.getItems().add("--- АКТИВНЫЕ ЗАКОНЫ ---");
            for (Law law : active) {
                lawListView.getItems().add("✅ " + law.getName() + " (" + getBonusString(law) + ")");
            }
            lawListView.getItems().add("");
        }

        if (!available.isEmpty()) {
            lawListView.getItems().add("--- ДОСТУПНЫЕ ЗАКОНЫ ---");
            for (Law law : available) {
                lawListView.getItems().add(law.getName() + " (" + getBonusString(law) + ")");
            }
        }

        if (active.isEmpty() && available.isEmpty()) {
            lawListView.getItems().add("Нет доступных или активных законов");
        }
    }

    public VBox getPanel() {
        return panel;
    }
}
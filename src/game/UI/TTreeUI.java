package game.UI;

import engine.infrastructure.GameState;
import game.model.research.TechNode;
import game.model.research.TechTree;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class TTreeUI {
    public static StackPane overlayRoot;

    private TechTree techTree;
    private GameState gameState;
    private Runnable onClose;
    private Runnable onStatusUpdate;

    public void setData(TechTree techTree, GameState gameState, Runnable onClose, Runnable onStatusUpdate) {
        this.techTree = techTree;
        this.gameState = gameState;
        this.onClose = onClose;
        this.onStatusUpdate = onStatusUpdate;
    }

    public void showTree(String type) {
        if (overlayRoot == null) return;
        overlayRoot.setVisible(true);
        overlayRoot.getChildren().clear();

        // ИСПРАВЛЕНО: используем techTree.isReligionUnlocked() вместо gameState
        if ("religion".equals(type) && !techTree.isReligionUnlocked()) {
            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);
            content.setStyle("-fx-background-color: rgba(30,30,40,0.95); -fx-padding: 20; -fx-border-radius: 10;");
            content.setMaxWidth(400);

            Label blockedLabel = new Label("🔒 Религия ещё не доступна");
            blockedLabel.setTextFill(Color.RED);
            blockedLabel.setFont(Font.font(18));
            blockedLabel.setStyle("-fx-font-weight: bold;");

            Label hintLabel = new Label("Изучите культурную технологию 'Высшая воля', чтобы открыть религиозную ветку.");
            hintLabel.setTextFill(Color.LIGHTGRAY);
            hintLabel.setFont(Font.font(14));
            hintLabel.setWrapText(true);

            Button closeBtn = new Button("Закрыть");
            closeBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #555; -fx-text-fill: white;");
            closeBtn.setOnAction(e -> {
                overlayRoot.setVisible(false);
                if (onClose != null) onClose.run();
            });

            content.getChildren().addAll(blockedLabel, hintLabel, closeBtn);
            overlayRoot.getChildren().add(content);
            StackPane.setAlignment(content, Pos.CENTER);
            return;
        }

        // ... остальной код (отображение дерева) без изменений ...
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: rgba(30,30,40,0.95); -fx-padding: 20; -fx-border-radius: 10;");
        content.setMaxWidth(500);

        Label title = new Label();
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(18));
        title.setStyle("-fx-font-weight: bold;");

        VBox listBox = new VBox(5);
        listBox.setAlignment(Pos.CENTER);

        List<TechNode> nodes;
        if ("tech".equals(type)) {
            title.setText("Древо технологий");
            nodes = techTree.getTechs();
        } else if ("social".equals(type)) {
            title.setText("Социальные институты");
            nodes = techTree.getSocials();
        } else {
            title.setText("Религии");
            nodes = techTree.getReligions();
        }

        for (TechNode node : nodes) {
            HBox item = new HBox(10);
            Label nameLabel = new Label(node.getName() + " (" + node.getCost() + " очков)");
            nameLabel.setTextFill(node.isResearched() ? Color.GREEN : Color.WHITE);
            nameLabel.setStyle("-fx-font-size: 13px;");
            Button researchBtn = new Button("Изучить");
            researchBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #2a5a2a; -fx-text-fill: white;");
            researchBtn.setOnAction(e -> {
                if (node.isResearched()) {
                    if (onStatusUpdate != null) onStatusUpdate.run();
                    return;
                }
                boolean hasPrereqs = true;
                for (String prereq : node.getPrerequisites()) {
                    boolean found = false;
                    for (TechNode n : nodes) {
                        if (n.getName().equals(prereq) && n.isResearched()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        hasPrereqs = false;
                        break;
                    }
                }
                if (!hasPrereqs) {
                    if (onStatusUpdate != null) onStatusUpdate.run();
                    return;
                }
                if (gameState.getScience() >= node.getCost()) {
                    gameState.addScience(-node.getCost());
                    node.setResearched(true);
                    if (onStatusUpdate != null) onStatusUpdate.run();
                    showTree(type);
                } else {
                    if (onStatusUpdate != null) onStatusUpdate.run();
                }
            });
            if (node.isResearched()) {
                researchBtn.setVisible(false);
            }
            item.getChildren().addAll(nameLabel, researchBtn);
            listBox.getChildren().add(item);
        }

        Button closeBtn = new Button("Закрыть");
        closeBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #555; -fx-text-fill: white;");
        closeBtn.setOnAction(e -> {
            overlayRoot.setVisible(false);
            if (onClose != null) onClose.run();
        });

        content.getChildren().addAll(title, listBox, closeBtn);
        overlayRoot.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER);
    }
}
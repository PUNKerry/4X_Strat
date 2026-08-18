package game.UI;

import game.controller.GameController;
import game.model.research.TechNode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Управление панелью исследований в UIManager.
 * Отвечает за отображение текущих исследований и прогресса.
 */
public class ResearchPanelManager {

    private final GameController controller;
    private final UIManager uiManager;

    private VBox researchPanel;
    private VBox techBlock;
    private VBox socialBlock;
    private VBox religionBlock;

    private Label techNameLabel;
    private Label socialNameLabel;
    private Label religionNameLabel;
    private ProgressBar techProgressBar;
    private ProgressBar socialProgressBar;
    private ProgressBar religionProgressBar;
    private Label techTurnsLabel;
    private Label socialTurnsLabel;
    private Label religionTurnsLabel;
    private Button techTreeButton;
    private Button socialTreeButton;
    private Button religionTreeButton;

    public ResearchPanelManager(GameController controller, UIManager uiManager) {
        this.controller = controller;
        this.uiManager = uiManager;
        this.researchPanel = createResearchPanel();
    }

    // ========================================================================
    // Создание панели исследований
    // ========================================================================

    private VBox createResearchPanel() {
        VBox panel = new VBox(6);
        panel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75); -fx-padding: 8; -fx-background-radius: 5;");
        panel.setMaxWidth(240);
        panel.setPrefWidth(240);
        panel.setMaxHeight(Region.USE_PREF_SIZE);

        Label title = new Label("ИССЛЕДОВАНИЯ");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-underline: true;");

        techBlock = createTechBlock();
        socialBlock = createSocialBlock();
        religionBlock = createReligionBlock();

        religionBlock.setVisible(false);

        panel.getChildren().addAll(title, techBlock, socialBlock, religionBlock);
        return panel;
    }

    private VBox createTechBlock() {
        VBox block = new VBox(2);
        block.setStyle("-fx-background-color: rgba(40, 40, 50, 0.8); -fx-padding: 4; -fx-border-radius: 3; -fx-background-radius: 3;");

        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);

        Label techLabel = new Label("🔬");
        techLabel.setTextFill(Color.CYAN);

        techNameLabel = new Label("нет");
        techNameLabel.setTextFill(Color.WHITE);
        techNameLabel.setStyle("-fx-font-size: 11px;");

        techTreeButton = new Button("📖");
        techTreeButton.setStyle("-fx-font-size: 9px; -fx-padding: 0 4; -fx-background-color: #2a4a2a; -fx-text-fill: white;");
        techTreeButton.setOnAction(e -> uiManager.showTechTreeOverlay("tech"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        techTurnsLabel = new Label("—");
        techTurnsLabel.setTextFill(Color.LIGHTGRAY);
        techTurnsLabel.setStyle("-fx-font-size: 10px;");

        header.getChildren().addAll(techLabel, techNameLabel, spacer, techTurnsLabel, techTreeButton);

        techProgressBar = new ProgressBar(0);
        techProgressBar.setPrefWidth(Double.MAX_VALUE);
        techProgressBar.setMaxHeight(6);

        block.getChildren().addAll(header, techProgressBar);
        return block;
    }

    private VBox createSocialBlock() {
        VBox block = new VBox(2);
        block.setStyle("-fx-background-color: rgba(40, 40, 50, 0.8); -fx-padding: 4; -fx-border-radius: 3; -fx-background-radius: 3;");

        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);

        Label socialLabel = new Label("🎭");
        socialLabel.setTextFill(Color.MAGENTA);

        socialNameLabel = new Label("нет");
        socialNameLabel.setTextFill(Color.WHITE);
        socialNameLabel.setStyle("-fx-font-size: 11px;");

        socialTreeButton = new Button("📖");
        socialTreeButton.setStyle("-fx-font-size: 9px; -fx-padding: 0 4; -fx-background-color: #2a4a2a; -fx-text-fill: white;");
        socialTreeButton.setOnAction(e -> uiManager.showTechTreeOverlay("social"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        socialTurnsLabel = new Label("—");
        socialTurnsLabel.setTextFill(Color.LIGHTGRAY);
        socialTurnsLabel.setStyle("-fx-font-size: 10px;");

        header.getChildren().addAll(socialLabel, socialNameLabel, spacer, socialTurnsLabel, socialTreeButton);

        socialProgressBar = new ProgressBar(0);
        socialProgressBar.setPrefWidth(Double.MAX_VALUE);
        socialProgressBar.setMaxHeight(6);

        block.getChildren().addAll(header, socialProgressBar);
        return block;
    }

    private VBox createReligionBlock() {
        VBox block = new VBox(2);
        block.setStyle("-fx-background-color: rgba(40, 40, 50, 0.8); -fx-padding: 4; -fx-border-radius: 3; -fx-background-radius: 3;");

        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);

        Label religionLabel = new Label("🙏");
        religionLabel.setTextFill(Color.LAVENDER);

        religionNameLabel = new Label("нет");
        religionNameLabel.setTextFill(Color.WHITE);
        religionNameLabel.setStyle("-fx-font-size: 11px;");

        religionTreeButton = new Button("📖");
        religionTreeButton.setStyle("-fx-font-size: 9px; -fx-padding: 0 4; -fx-background-color: #2a4a2a; -fx-text-fill: white;");
        religionTreeButton.setOnAction(e -> uiManager.showTechTreeOverlay("religion"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        religionTurnsLabel = new Label("—");
        religionTurnsLabel.setTextFill(Color.LIGHTGRAY);
        religionTurnsLabel.setStyle("-fx-font-size: 10px;");

        header.getChildren().addAll(religionLabel, religionNameLabel, spacer, religionTurnsLabel, religionTreeButton);

        religionProgressBar = new ProgressBar(0);
        religionProgressBar.setPrefWidth(Double.MAX_VALUE);
        religionProgressBar.setMaxHeight(6);

        block.getChildren().addAll(header, religionProgressBar);
        return block;
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public VBox getResearchPanel() {
        return researchPanel;
    }

    public boolean isReligionBlockVisible() {
        return religionBlock.isVisible();
    }

    public void setReligionBlockVisible(boolean visible) {
        religionBlock.setVisible(visible);
    }

    // ========================================================================
    // Обновление панели исследований
    // ========================================================================

    public void updateResearchPanel() {
        TechNode tech = controller.getCurrentTech();
        TechNode social = controller.getCurrentSocial();
        TechNode religion = controller.getCurrentReligion();

        updateTechBlock(tech);
        updateSocialBlock(social);
        updateReligionBlock(religion);
    }

    private void updateTechBlock(TechNode tech) {
        if (tech != null) {
            techNameLabel.setText(tech.getName());
            techProgressBar.setProgress(controller.getTechProgress());
            int turns = controller.getTechTurnsLeft();
            techTurnsLabel.setText((turns == Integer.MAX_VALUE) ? "∞" : turns + "т");
        } else {
            techNameLabel.setText("нет");
            techProgressBar.setProgress(0);
            techTurnsLabel.setText("—");
        }
    }

    private void updateSocialBlock(TechNode social) {
        if (social != null) {
            socialNameLabel.setText(social.getName());
            socialProgressBar.setProgress(controller.getSocialProgress());
            int turns = controller.getSocialTurnsLeft();
            socialTurnsLabel.setText((turns == Integer.MAX_VALUE) ? "∞" : turns + "т");
        } else {
            socialNameLabel.setText("нет");
            socialProgressBar.setProgress(0);
            socialTurnsLabel.setText("—");
        }
    }

    private void updateReligionBlock(TechNode religion) {
        if (controller.isReligionUnlocked()) {
            religionBlock.setVisible(true);
            if (religion != null) {
                religionNameLabel.setText(religion.getName());
                religionProgressBar.setProgress(controller.getReligionProgress());
                int turns = controller.getReligionTurnsLeft();
                religionTurnsLabel.setText((turns == Integer.MAX_VALUE) ? "∞" : turns + "т");
            } else {
                religionNameLabel.setText("нет");
                religionProgressBar.setProgress(0);
                religionTurnsLabel.setText("—");
            }
        } else {
            religionBlock.setVisible(false);
        }
    }
}
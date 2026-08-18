package game.UI;

import game.controller.GameController;
import game.model.city.City;
import game.model.city.District;
import game.model.registry.*;
import game.model.research.TechTree;
import game.model.unit.*;
import game.model.world.Hex;
import game.model.world.Improvement;
import game.model.world.Tile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Set;

public class UIPanelManager {

    private final GameController controller;
    private final UIManager uiManager;
    private final TechRegistry techRegistry;
    private final ImprovementRegistry improvementRegistry;
    private final DistrictRegistry districtRegistry;
    private final CenterImprovementRegistry centerRegistry;

    private VBox progressPanel;
    private VBox unitPanel;
    private VBox cityPanel;
    private VBox infoPanel;
    private VBox cityInfoPanel;

    // Компоненты панели юнита
    private Label unitInfoLabel;
    private Label unitOwnerLabel;
    private Label unitMovementLabel;
    private Label unitActionLabel;
    private Label unitPopulationLabel;
    private Label unitFatigueLabel;
    private Label unitRestStatusLabel;
    private Button skipTurnButton;
    private Button actionButton;
    private Button disbandButton;
    private Button restButton;
    private Label waypointInfoLabel;
    private Button confirmWaypointButton;
    private Button cancelWaypointButton;
    private Button closeUnitPanelButton;

    // Компоненты панели города
    private Label cityNameLabel;
    private Label cityTypeLabel;
    private Label cityPopulationLabel;
    private Label cityHappinessLabel;
    private Label cityFoodLabel;
    private Label cityProductionLabel;
    private Label cityProgressLabel;
    private Label cityScienceLabel;
    private Label cityFaithLabel;
    private Label cityCultureLabel;
    private Label cityFreeWorkersLabel;
    private Button cityCloseButton;
    private Button assignButton;
    private Slider allocationSlider;
    private Label allocationLabel;
    private Label housingLabel;
    private ProgressBar expansionProgress;
    private Label expansionLabel;

    private ListView<UIManager.ProductionButtonItem> unitListView;
    private ListView<UIManager.ProductionButtonItem> improvementListView;
    private ListView<UIManager.ProductionButtonItem> projectListView;
    private ListView<UIManager.ProductionButtonItem> districtListView;

    // Инфопанель
    private Label infoCoordLabel;
    private Label infoTerrainLabel;
    private Label infoWaterLabel;
    private Label infoOwnerLabel;
    private Label infoFoodLabel;
    private Label infoProdLabel;
    private Label infoGoldLabel;
    private Label infoFaithLabel;
    private Label infoCultureLabel;
    private Label infoResourcesLabel;
    private Label infoImprovementsLabel;

    // Сводка города (вкладки)
    private ToggleGroup infoModeGroup;
    private RadioButton modeReligionButton;
    private RadioButton modeImprovementsButton;
    private VBox infoContentVBox;

    // ========================================================================
    // Конструктор
    // ========================================================================

    public UIPanelManager(GameController controller, UIManager uiManager,
                          TechRegistry techRegistry,
                          ImprovementRegistry improvementRegistry,
                          DistrictRegistry districtRegistry,
                          CenterImprovementRegistry centerRegistry) {
        this.controller = controller;
        this.uiManager = uiManager;
        this.techRegistry = techRegistry;
        this.improvementRegistry = improvementRegistry;
        this.districtRegistry = districtRegistry;
        this.centerRegistry = centerRegistry;
        createPanels();
    }

    // ========================================================================
    // Геттеры панелей
    // ========================================================================

    public VBox getProgressPanel() { return progressPanel; }
    public VBox getUnitPanel() { return unitPanel; }
    public VBox getCityPanel() { return cityPanel; }
    public VBox getInfoPanel() { return infoPanel; }
    public VBox getCityInfoPanel() { return cityInfoPanel; }

    // ========================================================================
    // Управление видимостью
    // ========================================================================

    public void showUnitPanel(boolean show) { unitPanel.setVisible(show); }
    public void showCityPanel(boolean show) { cityPanel.setVisible(show); }
    public void showInfoPanel(boolean show) { infoPanel.setVisible(show); }
    public void showCityInfoPanel(boolean show) { cityInfoPanel.setVisible(show); }
    public void showProgressPanel(boolean show) { progressPanel.setVisible(show); }

    // ========================================================================
    // Создание панелей
    // ========================================================================

    private void createPanels() {
        progressPanel = createProgressPanel();
        unitPanel = createUnitPanel();
        cityPanel = createCityPanel();
        infoPanel = createInfoPanel();
        cityInfoPanel = createCityInfoPanel();
    }

    private VBox createProgressPanel() {
        VBox panel = new VBox(6);
        panel.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 5;");
        panel.setMaxWidth(220);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        return panel;
    }

    // ========================================================================
    // UNIT PANEL
    // ========================================================================

    private VBox createUnitPanel() {
        VBox panel = new VBox(8);
        panel.setStyle("-fx-background-color: rgba(50, 50, 50, 0.9); -fx-border-color: #888; -fx-border-width: 2; -fx-padding: 10; -fx-background-radius: 8;");
        panel.setMaxWidth(240);

        unitInfoLabel = new Label("Юнит: Поселенец");
        unitInfoLabel.setTextFill(Color.WHITE);
        unitInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        unitOwnerLabel = new Label("Владелец: Игрок");
        unitOwnerLabel.setTextFill(Color.LIGHTGRAY);
        unitOwnerLabel.setStyle("-fx-font-size: 12px;");

        unitMovementLabel = new Label("Очки движения: 3/3");
        unitMovementLabel.setTextFill(Color.LIGHTGREEN);
        unitMovementLabel.setStyle("-fx-font-size: 12px;");

        unitActionLabel = new Label("Очки действия: 1/1");
        unitActionLabel.setTextFill(Color.LIGHTCYAN);
        unitActionLabel.setStyle("-fx-font-size: 12px;");

        unitPopulationLabel = new Label("👤 Население: 500");
        unitPopulationLabel.setTextFill(Color.LIGHTYELLOW);
        unitPopulationLabel.setStyle("-fx-font-size: 12px;");

        unitFatigueLabel = new Label("😩 Усталость: 0");
        unitFatigueLabel.setTextFill(Color.LIGHTPINK);
        unitFatigueLabel.setStyle("-fx-font-size: 12px;");

        unitRestStatusLabel = new Label("⛺ Отдых: Нет");
        unitRestStatusLabel.setTextFill(Color.LIGHTGRAY);
        unitRestStatusLabel.setStyle("-fx-font-size: 12px;");

        waypointInfoLabel = new Label("");
        waypointInfoLabel.setTextFill(Color.rgb(255, 215, 0));
        waypointInfoLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        waypointInfoLabel.setWrapText(true);

        confirmWaypointButton = new Button("Подтвердить маршрут");
        confirmWaypointButton.setStyle("-fx-font-size: 12px; -fx-background-color: #2a7a2a; -fx-text-fill: white;");
        confirmWaypointButton.setOnAction(e -> controller.confirmWaypoint());
        confirmWaypointButton.setVisible(false);

        cancelWaypointButton = new Button("Отменить маршрут");
        cancelWaypointButton.setStyle("-fx-font-size: 12px; -fx-background-color: #8b4500; -fx-text-fill: white;");
        cancelWaypointButton.setOnAction(e -> {
            controller.cancelWaypointForSelectedUnit();
            updateUnitPanel(controller.getSelectedUnit());
        });
        cancelWaypointButton.setVisible(false);

        closeUnitPanelButton = new Button("Закрыть панель");
        closeUnitPanelButton.setStyle("-fx-font-size: 12px; -fx-background-color: #555; -fx-text-fill: white;");
        closeUnitPanelButton.setOnAction(e -> {
            controller.clearHighlights();
            controller.selectUnit(null);
            panel.setVisible(false);
        });

        skipTurnButton = new Button("Пропустить ход");
        skipTurnButton.setStyle("-fx-font-size: 12px; -fx-background-color: #555; -fx-text-fill: white;");
        skipTurnButton.setOnAction(e -> {
            controller.clearHighlights();
            controller.selectUnit(null);
            panel.setVisible(false);
        });

        actionButton = new Button("Основать город");
        actionButton.setStyle("-fx-font-size: 12px; -fx-background-color: #2a7a2a; -fx-text-fill: white;");
        actionButton.setOnAction(e -> {
            Unit unit = controller.getSelectedUnit();
            if (unit != null && unit.canFoundCity() && unit.canAct()) {
                Hex center = unit.getCurrentHex();
                if (controller.findCityAtHex(center) == null) {
                    uiManager.getCityNameInputOverlay().setVisible(true);
                    uiManager.getCityNameField().requestFocus();
                    uiManager.getCityNameField().selectAll();
                } else {
                    uiManager.updateStatus("Здесь уже есть город!");
                }
            } else {
                uiManager.updateStatus("Недостаточно очков действия.");
            }
        });

        restButton = new Button("Отдохнуть");
        restButton.setStyle("-fx-font-size: 12px; -fx-background-color: #4a6a8a; -fx-text-fill: white;");
        restButton.setOnAction(e -> {
            Unit unit = controller.getSelectedUnit();
            if (unit != null) {
                controller.restUnit(unit);
                uiManager.updateUnitPanel(unit);
            }
        });

        disbandButton = new Button("Распустить юнит");
        disbandButton.setStyle("-fx-font-size: 12px; -fx-background-color: #8b0000; -fx-text-fill: white;");
        disbandButton.setOnAction(e -> {
            Unit unit = controller.getSelectedUnit();
            if (unit != null) {
                controller.disbandUnit(unit);
                panel.setVisible(false);
            }
        });

        panel.getChildren().addAll(
                unitInfoLabel, unitOwnerLabel, unitMovementLabel, unitActionLabel,
                unitPopulationLabel, unitFatigueLabel, unitRestStatusLabel,
                waypointInfoLabel, confirmWaypointButton, cancelWaypointButton, closeUnitPanelButton,
                skipTurnButton, actionButton, restButton, disbandButton
        );
        return panel;
    }

    // ========================================================================
    // CITY PANEL
    // ========================================================================

    private VBox createCityPanel() {
        VBox panel = new VBox(8);
        panel.setStyle("-fx-background-color: rgba(50, 50, 50, 0.9); -fx-border-color: #888; -fx-border-width: 2; -fx-padding: 10; -fx-background-radius: 8;");
        panel.setMaxWidth(420);
        panel.setPrefWidth(420);

        cityNameLabel = new Label("Город: Название");
        cityNameLabel.setTextFill(Color.WHITE);
        cityNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        cityTypeLabel = new Label("Тип: Поселение");
        cityTypeLabel.setTextFill(Color.LIGHTYELLOW);
        cityTypeLabel.setStyle("-fx-font-size: 13px;");

        VBox infoTop = new VBox(4);
        cityPopulationLabel = new Label("Население: 1");
        cityPopulationLabel.setTextFill(Color.LIGHTGREEN);
        cityPopulationLabel.setStyle("-fx-font-size: 13px;");
        cityPopulationLabel.setWrapText(true);

        cityHappinessLabel = new Label("Довольство: 50");
        cityHappinessLabel.setTextFill(Color.LIGHTCYAN);
        cityHappinessLabel.setStyle("-fx-font-size: 13px;");
        cityHappinessLabel.setWrapText(true);

        cityFreeWorkersLabel = new Label("👷 Свободные рабочие: 0");
        cityFreeWorkersLabel.setTextFill(Color.LIGHTGOLDENRODYELLOW);
        cityFreeWorkersLabel.setStyle("-fx-font-size: 13px;");
        cityFreeWorkersLabel.setWrapText(true);

        infoTop.getChildren().addAll(cityPopulationLabel, cityHappinessLabel, cityFreeWorkersLabel);

        VBox resourcesBox = new VBox(4);
        cityFoodLabel = new Label("🍖 Еда: 0");
        cityFoodLabel.setTextFill(Color.LIGHTGREEN);
        cityFoodLabel.setStyle("-fx-font-size: 13px;");
        cityFoodLabel.setWrapText(true);

        cityProductionLabel = new Label("⚙️ Производство: 0");
        cityProductionLabel.setTextFill(Color.LIGHTSALMON);
        cityProductionLabel.setStyle("-fx-font-size: 13px;");
        cityProductionLabel.setWrapText(true);

        cityScienceLabel = new Label("🔬 Наука: 0");
        cityScienceLabel.setTextFill(Color.CYAN);
        cityScienceLabel.setStyle("-fx-font-size: 13px;");
        cityScienceLabel.setWrapText(true);

        cityFaithLabel = new Label("🙏 Вера: 0");
        cityFaithLabel.setTextFill(Color.LAVENDER);
        cityFaithLabel.setStyle("-fx-font-size: 13px;");
        cityFaithLabel.setWrapText(true);

        cityCultureLabel = new Label("🎭 Культура: 0");
        cityCultureLabel.setTextFill(Color.MAGENTA);
        cityCultureLabel.setStyle("-fx-font-size: 13px;");
        cityCultureLabel.setWrapText(true);

        resourcesBox.getChildren().addAll(cityFoodLabel, cityProductionLabel, cityScienceLabel, cityFaithLabel, cityCultureLabel);

        VBox housingBox = new VBox(4);
        housingLabel = new Label("🏠 Жильё: 0 / 1000");
        housingLabel.setTextFill(Color.LIGHTYELLOW);
        housingLabel.setStyle("-fx-font-size: 13px;");
        housingLabel.setWrapText(true);

        expansionProgress = new ProgressBar(0);
        expansionProgress.setPrefWidth(150);
        expansionProgress.setMaxHeight(8);
        expansionLabel = new Label("Расширение: 0/10 ходов");
        expansionLabel.setTextFill(Color.LIGHTGRAY);
        expansionLabel.setStyle("-fx-font-size: 11px;");
        housingBox.getChildren().addAll(housingLabel, expansionProgress, expansionLabel);

        cityProgressLabel = new Label("Прогресс: -");
        cityProgressLabel.setTextFill(Color.LIGHTBLUE);
        cityProgressLabel.setStyle("-fx-font-size: 12px;");
        cityProgressLabel.setWrapText(true);

        Separator sep1 = new Separator();

        Label unitTitle = new Label("Юниты");
        unitTitle.setTextFill(Color.WHITE);
        unitTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        unitListView = new ListView<>();
        unitListView.setPrefHeight(100);
        unitListView.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white; -fx-font-size: 12px;");
        unitListView.setCellFactory(list -> new ListCell<UIManager.ProductionButtonItem>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-font-size: 12px; -fx-background-color: #2a7a2a; -fx-text-fill: white; -fx-padding: 2 8;");
                btn.setOnAction(e -> {
                    UIManager.ProductionButtonItem item = getItem();
                    if (item != null && item.action != null) {
                        item.action.run();
                    }
                });
            }
            @Override
            protected void updateItem(UIManager.ProductionButtonItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    btn.setText(item.text);
                    btn.setDisable(!item.available);
                    Tooltip tooltip = new Tooltip(item.tooltipText);
                    tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #333; -fx-text-fill: white; -fx-padding: 8;");
                    btn.setTooltip(tooltip);
                    setGraphic(btn);
                }
            }
        });

        Label improvementTitle = new Label("Улучшения");
        improvementTitle.setTextFill(Color.WHITE);
        improvementTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        improvementListView = new ListView<>();
        improvementListView.setPrefHeight(200);
        improvementListView.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white; -fx-font-size: 12px;");
        improvementListView.setCellFactory(list -> new ListCell<UIManager.ProductionButtonItem>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-font-size: 12px; -fx-background-color: #7a5a2a; -fx-text-fill: white; -fx-padding: 2 8;");
                btn.setOnAction(e -> {
                    UIManager.ProductionButtonItem item = getItem();
                    if (item != null && item.action != null) {
                        item.action.run();
                    }
                });
            }
            @Override
            protected void updateItem(UIManager.ProductionButtonItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    btn.setText(item.text);
                    btn.setDisable(!item.available);
                    Tooltip tooltip = new Tooltip(item.tooltipText);
                    tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #333; -fx-text-fill: white; -fx-padding: 8;");
                    btn.setTooltip(tooltip);
                    setGraphic(btn);
                }
            }
        });

        Label districtTitle = new Label("Районы");
        districtTitle.setTextFill(Color.WHITE);
        districtTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        districtListView = new ListView<>();
        districtListView.setPrefHeight(100);
        districtListView.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white; -fx-font-size: 12px;");
        districtListView.setCellFactory(list -> new ListCell<UIManager.ProductionButtonItem>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-font-size: 12px; -fx-background-color: #4a6a7a; -fx-text-fill: white; -fx-padding: 2 8;");
                btn.setOnAction(e -> {
                    UIManager.ProductionButtonItem item = getItem();
                    if (item != null && item.action != null) {
                        item.action.run();
                    }
                });
            }
            @Override
            protected void updateItem(UIManager.ProductionButtonItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    btn.setText(item.text);
                    btn.setDisable(!item.available);
                    Tooltip tooltip = new Tooltip(item.tooltipText);
                    tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #333; -fx-text-fill: white; -fx-padding: 8;");
                    btn.setTooltip(tooltip);
                    setGraphic(btn);
                }
            }
        });

        Label projectTitle = new Label("Проекты");
        projectTitle.setTextFill(Color.WHITE);
        projectTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        projectListView = new ListView<>();
        projectListView.setPrefHeight(70);
        projectListView.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white; -fx-font-size: 12px;");
        projectListView.setCellFactory(list -> new ListCell<UIManager.ProductionButtonItem>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-font-size: 12px; -fx-background-color: #6a4a7a; -fx-text-fill: white; -fx-padding: 2 8;");
                btn.setOnAction(e -> {
                    UIManager.ProductionButtonItem item = getItem();
                    if (item != null && item.action != null) {
                        item.action.run();
                    }
                });
            }
            @Override
            protected void updateItem(UIManager.ProductionButtonItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    btn.setText(item.text);
                    Tooltip tooltip = new Tooltip(item.tooltipText);
                    tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #333; -fx-text-fill: white; -fx-padding: 8;");
                    btn.setTooltip(tooltip);
                    setGraphic(btn);
                }
            }
        });
        projectListView.getItems().addAll(
                new UIManager.ProductionButtonItem("Проект 1",
                        () -> {
                            City city = controller.getSelectedCity();
                            if (city != null) {
                                controller.addProjectToCity(city, "Проект 1");
                            }
                            uiManager.updateCityPanel(city);
                        },
                        "Проект 1\nВременный бонус к производству +20% на 5 ходов.\nСтоимость: 40⚙\nТребуется: 100👤\nСодержание: 0💰/ход",
                        true
                ),
                new UIManager.ProductionButtonItem("Проект 2",
                        () -> {
                            City city = controller.getSelectedCity();
                            if (city != null) {
                                controller.addProjectToCity(city, "Проект 2");
                            }
                            uiManager.updateCityPanel(city);
                        },
                        "Проект 2\nВременный бонус к науке +20% на 5 ходов.\nСтоимость: 40⚙\nТребуется: 100👤\nСодержание: 0💰/ход",
                        true
                )
        );

        Separator sep2 = new Separator();

        HBox allocationBox = new HBox(5);
        allocationBox.setAlignment(Pos.CENTER_LEFT);
        allocationBox.setPadding(new Insets(5, 0, 5, 0));

        Label allocationTitle = new Label("Распределение:");
        allocationTitle.setTextFill(Color.WHITE);
        allocationTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label leftLabel = new Label("Юниты");
        leftLabel.setTextFill(Color.LIGHTGREEN);
        leftLabel.setStyle("-fx-font-size: 11px;");

        allocationSlider = new Slider(0, 100, 50);
        allocationSlider.setPrefWidth(140);
        allocationSlider.setStyle(
                "-fx-control-inner-background: #3a3a3a; " +
                        "-fx-background-color: #2a2a2a; " +
                        "-fx-accent: #4a9eff; " +
                        "-fx-track-color: #555;"
        );

        Label rightLabel = new Label("Постройки");
        rightLabel.setTextFill(Color.LIGHTSALMON);
        rightLabel.setStyle("-fx-font-size: 11px;");

        allocationLabel = new Label("50%");
        allocationLabel.setTextFill(Color.LIGHTBLUE);
        allocationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 40px;");

        allocationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int val = newVal.intValue();
            allocationLabel.setText(val + "%");
            City city = controller.getSelectedCity();
            if (city != null && !allocationSlider.isValueChanging()) {
                city.setProductionAllocation(val);
                uiManager.updateCityPanel(city);
            }
        });
        allocationSlider.setOnMouseReleased(e -> {
            City city = controller.getSelectedCity();
            if (city != null) {
                city.setProductionAllocation((int) allocationSlider.getValue());
                uiManager.updateCityPanel(city);
            }
        });

        allocationBox.getChildren().addAll(allocationTitle, leftLabel, allocationSlider, rightLabel, allocationLabel);

        assignButton = new Button("Назначить горожан");
        assignButton.setStyle("-fx-font-size: 12px; -fx-background-color: #2a5a7a; -fx-text-fill: white;");
        assignButton.setOnAction(e -> {
            City city = controller.getSelectedCity();
            if (city != null) {
                controller.toggleAssignmentMode();
                uiManager.updateCityPanel(city);
            }
        });

        cityCloseButton = new Button("Закрыть");
        cityCloseButton.setStyle("-fx-font-size: 12px; -fx-background-color: #555; -fx-text-fill: white;");
        cityCloseButton.setOnAction(e -> {
            controller.selectCity(null);
            panel.setVisible(false);
        });

        panel.getChildren().addAll(
                cityNameLabel,
                cityTypeLabel,
                infoTop,
                resourcesBox,
                housingBox,
                cityProgressLabel,
                sep1,
                unitTitle, unitListView,
                improvementTitle, improvementListView,
                districtTitle, districtListView,
                projectTitle, projectListView,
                sep2,
                allocationBox,
                assignButton,
                cityCloseButton
        );
        return panel;
    }

    // ========================================================================
    // INFO PANEL
    // ========================================================================

    private VBox createInfoPanel() {
        VBox panel = new VBox(4);
        panel.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-padding: 8; -fx-background-radius: 5;");
        panel.setMaxWidth(260);
        panel.setMaxHeight(Region.USE_PREF_SIZE);

        infoCoordLabel = new Label("Клетка: -");
        infoCoordLabel.setTextFill(Color.WHITE);
        infoCoordLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        infoTerrainLabel = new Label("Тип: -");
        infoTerrainLabel.setTextFill(Color.LIGHTGRAY);
        infoTerrainLabel.setStyle("-fx-font-size: 11px;");

        infoWaterLabel = new Label("Вода: -");
        infoWaterLabel.setTextFill(Color.LIGHTBLUE);
        infoWaterLabel.setStyle("-fx-font-size: 11px;");

        infoOwnerLabel = new Label("Принадлежность: -");
        infoOwnerLabel.setTextFill(Color.LIGHTGOLDENRODYELLOW);
        infoOwnerLabel.setStyle("-fx-font-size: 11px;");

        infoFoodLabel = new Label("🍖 Еда: -");
        infoFoodLabel.setTextFill(Color.LIGHTGREEN);
        infoFoodLabel.setStyle("-fx-font-size: 11px;");

        infoProdLabel = new Label("⚙️ Производство: -");
        infoProdLabel.setTextFill(Color.LIGHTSALMON);
        infoProdLabel.setStyle("-fx-font-size: 11px;");

        infoGoldLabel = new Label("💰 Золото: -");
        infoGoldLabel.setTextFill(Color.GOLD);
        infoGoldLabel.setStyle("-fx-font-size: 11px;");

        infoFaithLabel = new Label("🙏 Вера: -");
        infoFaithLabel.setTextFill(Color.LAVENDER);
        infoFaithLabel.setStyle("-fx-font-size: 11px;");

        infoCultureLabel = new Label("🎭 Культура: -");
        infoCultureLabel.setTextFill(Color.MAGENTA);
        infoCultureLabel.setStyle("-fx-font-size: 11px;");

        infoResourcesLabel = new Label("💎 Редкие ресурсы: -");
        infoResourcesLabel.setTextFill(Color.CYAN);
        infoResourcesLabel.setStyle("-fx-font-size: 11px;");

        infoImprovementsLabel = new Label("🏗️ Улучшения: -");
        infoImprovementsLabel.setTextFill(Color.ORANGE);
        infoImprovementsLabel.setStyle("-fx-font-size: 11px;");

        panel.getChildren().addAll(
                infoCoordLabel,
                infoTerrainLabel,
                infoWaterLabel,
                infoOwnerLabel,
                infoFoodLabel,
                infoProdLabel,
                infoGoldLabel,
                infoFaithLabel,
                infoCultureLabel,
                infoResourcesLabel,
                infoImprovementsLabel
        );
        return panel;
    }

    // ========================================================================
    // CITY INFO PANEL
    // ========================================================================

    private VBox createCityInfoPanel() {
        VBox panel = new VBox(8);
        panel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 10; -fx-background-radius: 5;");
        panel.setMaxWidth(280);
        panel.setPrefWidth(280);
        panel.setVisible(false);

        Label title = new Label("Сводка города");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        infoModeGroup = new ToggleGroup();
        modeReligionButton = new RadioButton("Религия и довольство");
        modeReligionButton.setToggleGroup(infoModeGroup);
        modeReligionButton.setSelected(true);
        modeReligionButton.setTextFill(Color.LIGHTGRAY);
        modeReligionButton.setStyle("-fx-font-size: 12px;");
        modeReligionButton.setOnAction(e -> uiManager.updateCityInfoPanel(controller.getSelectedCity()));

        modeImprovementsButton = new RadioButton("Улучшения и жители");
        modeImprovementsButton.setToggleGroup(infoModeGroup);
        modeImprovementsButton.setTextFill(Color.LIGHTGRAY);
        modeImprovementsButton.setStyle("-fx-font-size: 12px;");
        modeImprovementsButton.setOnAction(e -> uiManager.updateCityInfoPanel(controller.getSelectedCity()));

        HBox toggleBox = new HBox(10);
        toggleBox.getChildren().addAll(modeReligionButton, modeImprovementsButton);

        Separator sep = new Separator();

        infoContentVBox = new VBox(5);
        infoContentVBox.setStyle("-fx-padding: 5;");

        panel.getChildren().addAll(title, toggleBox, sep, infoContentVBox);
        return panel;
    }

    // ========================================================================
    // МЕТОДЫ ОБНОВЛЕНИЯ
    // ========================================================================

    public void updateUnitPanel(Unit unit) {
        if (unit == null) {
            unitPanel.setVisible(false);
            return;
        }

        String type = getUnitTypeName(unit);
        unitInfoLabel.setText("Юнит: " + type);
        unitOwnerLabel.setText("Владелец: " + unit.getOwner());
        unitMovementLabel.setText("Очки движения: " + unit.getMovementPoints() + "/" + unit.getMaxMovementPoints());
        unitActionLabel.setText("Очки действия: " + unit.getActionPoints() + "/" + unit.getMaxActionPoints());
        actionButton.setVisible(unit.canFoundCity());

        if (unit.getHomeCity() != null) {
            unitInfoLabel.setText(unitInfoLabel.getText() + " | 🏠" + unit.getHomeCity().getName());
        }

        unitFatigueLabel.setText("😩 Усталость: " + unit.getFatigue() + "%");
        unitFatigueLabel.setVisible(true);
        unitRestStatusLabel.setText("⛺ Отдых: " + (unit.isResting() ? "Да" : "Нет"));
        unitRestStatusLabel.setVisible(true);
        restButton.setVisible(true);

        if (unit.getPopulation() > 0) {
            unitPopulationLabel.setText("👤 Население: " + unit.getPopulation());
            unitPopulationLabel.setVisible(true);
        } else if (unit.getSquadMembers() > 0) {
            unitPopulationLabel.setText("👥 Члены отряда: " + unit.getSquadMembers());
            unitPopulationLabel.setVisible(true);
        } else {
            unitPopulationLabel.setVisible(false);
        }

        // Управление кнопками маршрута
        if (unit.isWaypointMode()) {
            int totalSteps = unit.getWaypoints().size() - 1;
            int stopCount = unit.getStopPoints().size();
            waypointInfoLabel.setText("📌 Маршрут: " + totalSteps + " кл., " + stopCount + " остановок");
            waypointInfoLabel.setVisible(true);
            confirmWaypointButton.setVisible(false);
            cancelWaypointButton.setVisible(true);
        } else if (controller.isWaypointPending()) {
            List<Hex> path = controller.getCurrentPath();
            if (path != null && !path.isEmpty()) {
                int steps = path.size() - 1;
                waypointInfoLabel.setText("📌 Путь: " + steps + " кл., " + controller.getCurrentStopPoints().size() + " остановок. Подтвердите.");
                waypointInfoLabel.setVisible(true);
                confirmWaypointButton.setVisible(true);
                cancelWaypointButton.setVisible(false);
            } else {
                waypointInfoLabel.setVisible(false);
                confirmWaypointButton.setVisible(false);
                cancelWaypointButton.setVisible(false);
            }
        } else {
            waypointInfoLabel.setVisible(false);
            confirmWaypointButton.setVisible(false);
            cancelWaypointButton.setVisible(false);
        }

        unitPanel.setVisible(true);
    }

    public void updateCityPanel(City city) {
        if (city == null) {
            cityPanel.setVisible(false);
            return;
        }

        cityNameLabel.setText("Город: " + city.getName());
        cityTypeLabel.setText("Тип: " + city.getType());
        int change = city.getPopulationChange();
        String changeStr = (change >= 0 ? "+" : "") + change;
        cityPopulationLabel.setText("Население: " + city.getPopulation() + " (" + changeStr + ")");
        cityHappinessLabel.setText("Довольство: " + city.getHappiness());

        int totalFood = city.calculateFood(controller.getWorld());
        int totalProd = city.calculateProduction(controller.getWorld());
        cityFoodLabel.setText("🍖 Еда: " + totalFood + " (назначено: " + city.getUsedCitizens() + "/" + city.getMaxCitizens() + ")");
        cityProductionLabel.setText("⚙️ Производство: " + totalProd);

        int free = city.getFreeWorkers();
        cityFreeWorkersLabel.setText("👷 Свободные рабочие: " + free + " ед. (" + (free * 50) + " чел.)");

        if (controller.isCityView() && controller.isAssignmentMode() && controller.getAssignmentCity() == city) {
            assignButton.setText("Выйти из режима назначения");
        } else {
            assignButton.setText("Назначить горожан");
        }

        if (!allocationSlider.isValueChanging()) {
            allocationSlider.setValue(city.getProductionAllocation());
        }
        allocationLabel.setText((int) allocationSlider.getValue() + "%");

        String progressText = "Прогресс: -";
        if (city.getProductionItem() != null) {
            int prog = city.getProductionProgress();
            int target = city.getProductionTarget();
            progressText = "Прогресс: " + prog + "/" + target;
            if (city.getProductionItem().startsWith("unit_")) {
                progressText += " (Юнит)";
            } else if (city.getProductionItem().startsWith("improvement_")) {
                progressText += " (Улучшение)";
            } else if (city.getProductionItem().startsWith("district_")) {
                progressText += " (Район)";
            } else if (city.getProductionItem().startsWith("center_")) {
                progressText += " (Центральное улучшение)";
            } else if (city.getProductionItem().startsWith("project_")) {
                progressText += " (Проект)";
            }
        }
        cityProgressLabel.setText(progressText);

        cityScienceLabel.setText("🔬 Наука: " + city.getScienceOutput());
        cityFaithLabel.setText("🙏 Вера: " + city.getFaithOutput());
        cityCultureLabel.setText("🎭 Культура: " + city.getCultureOutput());

        housingLabel.setText("🏠 Жильё: " + city.getPopulation() + " / " + city.getHousingCapacity());
        int timer = city.getExpansionTimer();
        double progress = Math.min(1.0, timer / 10.0);
        expansionProgress.setProgress(progress);
        expansionLabel.setText("Расширение: " + timer + "/10 ходов");

        updateUnitList(city);
        updateImprovementList(city);
        updateDistrictList(city);

        cityPanel.setVisible(true);
    }

    public void updateInfoPanel(Hex hex, Tile tile) {
        if (tile == null) {
            infoPanel.setVisible(false);
            return;
        }
        var info = controller.getTileInfo(hex);
        if (info == null) {
            infoPanel.setVisible(false);
            return;
        }
        infoCoordLabel.setText("Клетка: " + info.coord);
        infoTerrainLabel.setText("Тип: " + info.terrain);
        infoWaterLabel.setText("Вода: " + info.water);
        infoOwnerLabel.setText("Принадлежность: " + info.owner);
        infoFoodLabel.setText("🍖 Еда: " + info.food);
        infoProdLabel.setText("⚙️ Производство: " + info.production);
        infoGoldLabel.setText("💰 Золото: " + info.gold);
        infoFaithLabel.setText("🙏 Вера: " + info.faith);
        infoCultureLabel.setText("🎭 Культура: " + info.culture);
        infoResourcesLabel.setText("💎 Редкие ресурсы: " + info.resources);
        infoImprovementsLabel.setText("🏗️ Улучшения: " + info.improvements);
        infoPanel.setVisible(true);
    }

    public void updateCityInfoPanel(City city) {
        if (city == null || !controller.isCityView()) {
            cityInfoPanel.setVisible(false);
            return;
        }
        cityInfoPanel.setVisible(true);
        infoContentVBox.getChildren().clear();

        if (modeReligionButton.isSelected()) {
            Label religion = new Label("Религия: " + (controller.getCurrentReligion() != null ? controller.getCurrentReligion().getName() : "Нет"));
            religion.setTextFill(Color.LIGHTBLUE);
            Label happiness = new Label("Довольство: " + city.getHappiness());
            happiness.setTextFill(Color.LIGHTGREEN);
            int free = city.getFreeWorkers();
            Label unemployed = new Label("Безработные: " + free + " ед. (" + (free * 50) + " чел.)");
            unemployed.setTextFill(Color.LIGHTYELLOW);
            Label governor = new Label("Губернатор: Назначен автоматически");
            governor.setTextFill(Color.LIGHTGRAY);
            Label interests = new Label("Группы интересов: нет данных");
            interests.setTextFill(Color.LIGHTPINK);
            infoContentVBox.getChildren().addAll(religion, happiness, unemployed, governor, interests);
        } else {
            Label improvementsTitle = new Label("Построенные улучшения:");
            improvementsTitle.setTextFill(Color.WHITE);
            improvementsTitle.setStyle("-fx-font-weight: bold;");
            infoContentVBox.getChildren().add(improvementsTitle);

            boolean hasImprovements = false;
            for (Hex hex : city.getTiles()) {
                Improvement imp = city.getImprovementAt(hex);
                if (imp != null && !imp.isUnderConstruction()) {
                    hasImprovements = true;
                    String bonus = "";
                    if (imp.getFoodBonus() > 0) {
                        bonus += " +" + imp.getFoodBonus() + "🍖";
                    }
                    if (imp.getProductionBonus() > 0) {
                        bonus += " +" + imp.getProductionBonus() + "⚙";
                    }
                    Label impLabel = new Label("  " + imp.getType().name() + " (кл." + hex.col + "," + hex.row + ")" + bonus);
                    impLabel.setTextFill(Color.LIGHTGREEN);
                    infoContentVBox.getChildren().add(impLabel);
                }
            }
            if (!hasImprovements) {
                Label noImp = new Label("  Нет улучшений");
                noImp.setTextFill(Color.LIGHTGRAY);
                infoContentVBox.getChildren().add(noImp);
            }

            Label districtsTitle = new Label("Построенные районы:");
            districtsTitle.setTextFill(Color.WHITE);
            districtsTitle.setStyle("-fx-font-weight: bold;");
            infoContentVBox.getChildren().add(districtsTitle);

            Set<District> districts = city.getCompletedDistricts();
            if (districts.isEmpty()) {
                Label noDist = new Label("  Нет построенных районов");
                noDist.setTextFill(Color.LIGHTGRAY);
                infoContentVBox.getChildren().add(noDist);
            } else {
                for (District d : districts) {
                    Label distLabel = new Label("  " + d.getName() + (d.getLocation() != null ? " (кл." + d.getLocation().col + "," + d.getLocation().row + ")" : ""));
                    distLabel.setTextFill(Color.LIGHTBLUE);
                    infoContentVBox.getChildren().add(distLabel);
                }
            }

            Label assignedTitle = new Label("Назначенные жители:");
            assignedTitle.setTextFill(Color.WHITE);
            assignedTitle.setStyle("-fx-font-weight: bold;");
            infoContentVBox.getChildren().add(assignedTitle);

            boolean hasAssigned = false;
            for (Hex hex : city.getTiles()) {
                int count = city.getAssignedCount(hex);
                if (count > 0 && !hex.equals(city.getCenter())) {
                    hasAssigned = true;
                    Label assignLabel = new Label("  Кл." + hex.col + "," + hex.row + ": " + count + " ед. (" + (count * 50) + " чел.)");
                    assignLabel.setTextFill(Color.LIGHTCYAN);
                    infoContentVBox.getChildren().add(assignLabel);
                }
            }
            if (!hasAssigned) {
                Label noAssigned = new Label("  Нет назначений (кроме центра)");
                noAssigned.setTextFill(Color.LIGHTGRAY);
                infoContentVBox.getChildren().add(noAssigned);
            }
        }
    }

    // ========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ========================================================================

    private String getUnitTypeName(Unit unit) {
        if (unit instanceof Settler) return "Поселенец";
        if (unit instanceof Scout) return "Скаут";
        if (unit instanceof Warrior) return "Воин";
        if (unit instanceof Archer) return "Лучник";
        if (unit instanceof Chariot) return "Колесница";
        if (unit instanceof BronzeSwordsman) return "Бронзовый мечник";
        if (unit instanceof Horseman) return "Всадник";
        if (unit instanceof Galley) return "Галера";
        if (unit instanceof BatteringRam) return "Осадный таран";
        return "Юнит";
    }

    private void updateUnitList(City city) {
        unitListView.getItems().clear();
        String[] unitTypes = {"settler", "scout", "warrior", "archer", "chariot", "bronze_swordsman", "horseman", "galley", "battering_ram"};
        for (String type : unitTypes) {
            String requiredTech = Unit.getRequiredTech(type);
            if (requiredTech != null && !techRegistry.isResearched(requiredTech)) {
                continue;
            }
            String reason = city.getUnitAvailability(type, controller.getTechTree());
            if (reason == null) {
                addUnitItem(type, city);
            } else {
                addUnitItemDisabled(type, city, reason);
            }
        }
    }

    private void addUnitItem(String type, City city) {
        String displayName = getUnitDisplayName(type);
        String tooltip = getUnitTooltip(type);
        unitListView.getItems().add(new UIManager.ProductionButtonItem(
                displayName,
                () -> {
                    city.addUnit(type);
                    updateCityPanel(city);
                },
                tooltip,
                true
        ));
    }

    private void addUnitItemDisabled(String type, City city, String reason) {
        String displayName = getUnitDisplayName(type) + " ⛔ (" + reason + ")";
        String tooltip = getUnitTooltip(type) + "\n\n⛔ " + reason;
        unitListView.getItems().add(new UIManager.ProductionButtonItem(
                displayName,
                () -> uiManager.updateStatus("Недоступно: " + reason),
                tooltip,
                false
        ));
    }

    private String getUnitDisplayName(String type) {
        switch (type) {
            case "settler": return "Поселенец";
            case "scout": return "Скаут";
            case "warrior": return "Воин";
            case "archer": return "Лучник";
            case "chariot": return "Колесница";
            case "bronze_swordsman": return "Бронзовый мечник";
            case "horseman": return "Всадник";
            case "galley": return "Боевая галера";
            case "battering_ram": return "Осадный таран";
            default: return type;
        }
    }

    private String getUnitTooltip(String type) {
        switch (type) {
            case "settler": return "Поселенец\nОснование новых городов.\nСтоимость: 50⚙\nТребуется: 500👤\nСодержание: 0💰/ход";
            case "scout": return "Скаут\nРазведка.\nСтоимость: 30⚙\nТребуется: 50👤\nТребуется технология: Приручение собаки";
            case "warrior": return "Воин\nБазовый боевой юнит.\nСтоимость: 40⚙\nТребуется: Обработка кремня";
            case "archer": return "Лучник\nДальний бой.\nСтоимость: 50⚙\nТребуется: Лук и стрелы";
            case "chariot": return "Колесница\nСильный на равнине.\nСтоимость: 60⚙\nТребуется: Колесо (раннее) + Бронзовый сплав";
            case "bronze_swordsman": return "Бронзовый мечник\nМощный ближний бой.\nСтоимость: 70⚙\nТребуется: Бронзовый сплав";
            case "horseman": return "Всадник\nБыстрая кавалерия.\nСтоимость: 65⚙\nТребуется: Одомашнивание лошади";
            case "galley": return "Боевая галера\nМорской юнит.\nСтоимость: 80⚙\nТребуется: Мореходство";
            case "battering_ram": return "Осадный таран\nЭффективен против стен.\nСтоимость: 75⚙\nТребуется: Осадное дело";
            default: return "";
        }
    }

    // ========================================================================
    // Улучшения (клеточные и центральные) – исправлено effectively final
    // ========================================================================

    private void updateImprovementList(City city) {
        improvementListView.getItems().clear();

        for (ImprovementData data : improvementRegistry.getAll()) {
            boolean techAvailable = data.isTechAvailable(techRegistry);
            String reason = null;

            if (!techAvailable) {
                reason = "Требуется технология: " + data.getRequiredTech();
            } else {
                List<Hex> availableHexes = city.getAvailableTilesForImprovement(data.getType());
                if (availableHexes.isEmpty()) {
                    reason = "Нет подходящих клеток в территории города";
                }
            }

            final String finalReason = reason; // финальная копия для лямбды

            if (finalReason == null) {
                improvementListView.getItems().add(new UIManager.ProductionButtonItem(
                        data.getDisplayName(),
                        () -> {
                            if (controller.getSelectedCity() != null) {
                                controller.enterPlacementMode(controller.getSelectedCity(), data.getType());
                            }
                        },
                        buildImprovementTooltip(data),
                        true
                ));
            } else {
                improvementListView.getItems().add(new UIManager.ProductionButtonItem(
                        data.getDisplayName() + " ⛔ (" + finalReason + ")",
                        () -> uiManager.updateStatus("Недоступно: " + finalReason),
                        buildImprovementTooltip(data) + "\n\n⛔ " + finalReason,
                        false
                ));
            }
        }

        // Центральные улучшения
        for (CenterImprovementData data : centerRegistry.getAll()) {
            boolean techAvailable = data.isTechAvailable(techRegistry);
            boolean isBuilt = city.getCompletedCenterImprovements().contains(data.getName());
            if (!isBuilt && techAvailable) {
                improvementListView.getItems().add(new UIManager.ProductionButtonItem(
                        data.getName(),
                        () -> {
                            if (controller.getSelectedCity() != null) {
                                controller.getSelectedCity().addCenterImprovementToQueue(data.getName());
                                uiManager.updateStatus("'" + data.getName() + "' добавлено в очередь производства.");
                            }
                        },
                        data.getTooltip(),
                        true
                ));
            }
        }
    }

    private String buildImprovementTooltip(ImprovementData data) {
        StringBuilder sb = new StringBuilder();
        sb.append(data.getDisplayName()).append("\n");
        sb.append("Стоимость: ").append(data.getCost()).append("⚙\n");
        sb.append("Еда: +").append(data.getFoodBonus()).append("\n");
        sb.append("Производство: +").append(data.getProductionBonus()).append("\n");
        sb.append("Требует для стройки: ").append(data.getWorkersForConstruction() * 50).append(" чел.\n");
        sb.append("Требует для работы: ").append(data.getWorkersToOperate() * 50).append(" чел.\n");
        sb.append("Можно строить на: ");
        boolean first = true;
        for (var t : data.getAllowedTerrain()) {
            if (!first) sb.append(", ");
            sb.append(t.getName());
            first = false;
        }
        sb.append("\n");
        if (data.getRequiredTech() != null && !data.getRequiredTech().isEmpty()) {
            sb.append("Требуется: ").append(data.getRequiredTech());
        }
        return sb.toString();
    }

    // ========================================================================
    // Районы – исправлено effectively final
    // ========================================================================

    private void updateDistrictList(City city) {
        districtListView.getItems().clear();

        for (DistrictData data : districtRegistry.getAll()) {
            boolean techAvailable = data.isTechAvailable(techRegistry);
            String reason = null;

            if (!techAvailable) {
                reason = "Требуется технология: " + data.getRequiredTech();
            } else {
                List<Hex> availableHexes = city.getAvailableTilesForDistrict(data.getType());
                if (availableHexes.isEmpty()) {
                    reason = "Нет свободных клеток для района";
                }
            }

            final String finalReason = reason; // финальная копия для лямбды

            if (finalReason == null) {
                districtListView.getItems().add(new UIManager.ProductionButtonItem(
                        data.getDisplayName(),
                        () -> {
                            if (controller.getSelectedCity() != null) {
                                controller.enterDistrictPlacementMode(controller.getSelectedCity(), data.getType());
                            }
                        },
                        buildDistrictTooltip(data),
                        true
                ));
            } else {
                districtListView.getItems().add(new UIManager.ProductionButtonItem(
                        data.getDisplayName() + " ⛔ (" + finalReason + ")",
                        () -> uiManager.updateStatus("Недоступно: " + finalReason),
                        buildDistrictTooltip(data) + "\n\n⛔ " + finalReason,
                        false
                ));
            }
        }
    }

    private String buildDistrictTooltip(DistrictData data) {
        StringBuilder sb = new StringBuilder();
        sb.append(data.getDisplayName()).append("\n");
        sb.append("Стоимость: ").append(data.getCost()).append("⚙\n");
        if (data.getScienceBonus() > 0) sb.append("Наука: +").append(data.getScienceBonus()).append("\n");
        if (data.getCultureBonus() > 0) sb.append("Культура: +").append(data.getCultureBonus()).append("\n");
        if (data.getFaithBonus() > 0) sb.append("Вера: +").append(data.getFaithBonus()).append("\n");
        if (data.getHousingBonus() > 0) sb.append("Жильё: +").append(data.getHousingBonus()).append("\n");
        sb.append("Требует рабочих: ").append(data.getWorkersRequired()).append(" ед. (").append(data.getWorkersRequired() * 50).append(" чел.)\n");
        if (data.getRequiredTech() != null && !data.getRequiredTech().isEmpty()) {
            sb.append("Требуется: ").append(data.getRequiredTech());
        }
        return sb.toString();
    }
}
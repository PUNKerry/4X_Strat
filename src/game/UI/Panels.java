package game.UI;

import game.model.city.City;
import game.model.world.Hex;
import game.model.world.TerrainType;
import game.model.world.Tile;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


import static game.model.city.CityGlobal.findCityAtHex;

public class Panels {

    public static VBox progressPanel;
    public static VBox unitPanel;

    public static VBox cityPanel;

    public static VBox infoPanel;

    // --- Панели юнита и города (справа снизу) ---

    public static Label unitInfoLabel, unitOwnerLabel, unitMovementLabel, unitActionLabel;
    public static Label unitPopulationLabel, unitFatigueLabel, unitRestStatusLabel;
    public static Button skipTurnButton, actionButton, disbandButton, restButton;


    public static Label cityNameLabel, cityTypeLabel, cityPopulationLabel, cityHappinessLabel;
    public static Label cityFoodLabel, cityProductionLabel, cityProgressLabel;
    public static Button cityProduceSettlerButton;
    public static Button cityCloseButton;

    // --- Информационная панель (слева снизу) ---

    public static Label infoCoordLabel;
    public static Label infoTerrainLabel;
    public static Label infoWaterLabel;
    public static Label infoOwnerLabel;
    public static Label infoFoodLabel;
    public static Label infoProdLabel;
    public static Label infoGoldLabel;
    public static Label infoFaithLabel;
    public static Label infoCultureLabel;
    public static Label infoResourcesLabel;
    public static Label infoImprovementsLabel;


    public static void updateInfoPanel(Hex hex, Tile tile) {
        TerrainType terrain = tile.getTerrain();

        infoCoordLabel.setText("Клетка: (" + hex.col + ", " + hex.row + ")");
        infoTerrainLabel.setText("Тип: " + terrain.getName());

        String waterStatus;
        switch (terrain) {
            case RIVER:
                waterStatus = "Пресная вода (река)";
                break;
            case OCEAN:
                waterStatus = "Есть вода (море)";
                break;
            default:
                waterStatus = "Нет воды";
                break;
        }
        infoWaterLabel.setText("Вода: " + waterStatus);

        City ownerCity = findCityAtHex(hex);
        String owner = (ownerCity != null) ? ownerCity.getName() : "Ничья";
        infoOwnerLabel.setText("Принадлежность: " + owner);

        int food = terrain.getFood();
        int prod = terrain.getProduction();

        infoFoodLabel.setText("🍖 Еда: " + food);
        infoProdLabel.setText("⚙️ Производство: " + prod);
        infoGoldLabel.setText("💰 Золото: 0");
        infoFaithLabel.setText("🙏 Вера: 0");
        infoCultureLabel.setText("🎭 Культура: 0");
        infoResourcesLabel.setText("💎 Редкие ресурсы: Нет");
        infoImprovementsLabel.setText("🏗️ Улучшения: Нет");
    }

}

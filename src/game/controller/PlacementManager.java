package game.controller;

import game.model.city.City;
import game.model.city.District;
import game.model.world.Hex;
import game.model.world.Improvement;
import game.model.world.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Управление режимами размещения улучшений и районов.
 */
public class PlacementManager {

    private final GameController controller;

    // Режимы размещения улучшений
    private boolean placementMode = false;
    private Improvement.Type placementType = null;
    private City placementCity = null;
    private List<Hex> availablePlacementHexes = new ArrayList<>();

    // Режим размещения районов
    private boolean districtPlacementMode = false;
    private District.Type districtPlacementType = null;
    private City districtPlacementCity = null;
    private List<Hex> availableDistrictPlacementHexes = new ArrayList<>();

    public PlacementManager(GameController controller) {
        this.controller = controller;
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public boolean isPlacementMode() { return placementMode; }
    public boolean isDistrictPlacementMode() { return districtPlacementMode; }

    // ========================================================================
    // Размещение улучшений
    // ========================================================================

    public void enterPlacementMode(City city, Improvement.Type type) {
        if (city == null) return;
        if (!controller.isCityView() || city != controller.getZoomedCity()) {
            controller.updateStatus("Сначала войдите в городской вид этого города.");
            return;
        }
        if (districtPlacementMode) exitDistrictPlacementMode();
        placementMode = true;
        placementType = type;
        placementCity = city;

        availablePlacementHexes = city.getAvailableTilesForImprovement(type);
        for (Hex hex : availablePlacementHexes) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) tile.setHighlighted(true);
        }

        if (availablePlacementHexes.isEmpty()) {
            controller.updateStatus("Нет доступных клеток для строительства " + type.name().toLowerCase() + ".");
        } else {
            controller.updateStatus("Выберите клетку для строительства " + type.name().toLowerCase() +
                    " (доступно " + availablePlacementHexes.size() + " клеток).");
        }
        if (controller.onStatusChanged != null) controller.onStatusChanged.run();
        controller.updateUI();
    }

    public void exitPlacementMode() {
        placementMode = false;
        placementType = null;
        placementCity = null;

        for (Hex hex : availablePlacementHexes) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) tile.setHighlighted(false);
        }
        availablePlacementHexes.clear();

        if (controller.onStatusChanged != null) controller.onStatusChanged.run();
        controller.updateUI();
    }

    public void handlePlacementClick(Hex hex) {
        if (!placementMode || placementCity == null) return;
        if (!availablePlacementHexes.contains(hex)) {
            controller.updateStatus("Нельзя построить здесь.");
            return;
        }
        if (placementCity.startImprovement(hex, placementType, controller.getTechTree())) {
            exitPlacementMode();
            controller.updateStatus("Строительство улучшения начато.");
            if (controller.onCitySelected != null) controller.onCitySelected.run();
            controller.updateUI();
        } else {
            controller.updateStatus("Недостаточно жителей или ресурсов.");
        }
    }

    // ========================================================================
    // Размещение районов
    // ========================================================================

    public void enterDistrictPlacementMode(City city, District.Type type) {
        if (city == null) return;
        if (!controller.isCityView() || city != controller.getZoomedCity()) {
            controller.updateStatus("Сначала войдите в городской вид этого города.");
            return;
        }
        if (placementMode) exitPlacementMode();
        districtPlacementMode = true;
        districtPlacementType = type;
        districtPlacementCity = city;

        availableDistrictPlacementHexes = city.getAvailableTilesForDistrict(type);
        for (Hex hex : availableDistrictPlacementHexes) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) tile.setHighlighted(true);
        }

        if (availableDistrictPlacementHexes.isEmpty()) {
            controller.updateStatus("Нет доступных клеток для строительства " + type.name().toLowerCase() + ".");
        } else {
            controller.updateStatus("Выберите клетку для строительства " + type.name().toLowerCase() +
                    " (доступно " + availableDistrictPlacementHexes.size() + " клеток).");
        }
        if (controller.onStatusChanged != null) controller.onStatusChanged.run();
        controller.updateUI();
    }

    public void exitDistrictPlacementMode() {
        districtPlacementMode = false;
        districtPlacementType = null;
        districtPlacementCity = null;

        for (Hex hex : availableDistrictPlacementHexes) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) tile.setHighlighted(false);
        }
        availableDistrictPlacementHexes.clear();

        if (controller.onStatusChanged != null) controller.onStatusChanged.run();
        controller.updateUI();
    }

    public void handleDistrictPlacementClick(Hex hex) {
        if (!districtPlacementMode || districtPlacementCity == null) return;
        if (!availableDistrictPlacementHexes.contains(hex)) {
            controller.updateStatus("Нельзя построить здесь.");
            return;
        }
        if (districtPlacementCity.startDistrict(districtPlacementType, hex, controller.getTechTree())) {
            exitDistrictPlacementMode();
            controller.updateStatus("Строительство района начато.");
            if (controller.onCitySelected != null) controller.onCitySelected.run();
            controller.updateUI();
        } else {
            controller.updateStatus("Недостаточно жителей или ресурсов.");
        }
    }

    // ========================================================================
    // Полный сброс режимов (для новой игры или выхода из городского вида)
    // ========================================================================

    public void reset() {
        if (placementMode) exitPlacementMode();
        if (districtPlacementMode) exitDistrictPlacementMode();
    }

    // ========================================================================
    // Вспомогательные методы для SelectionManager (сброс подсветки)
    // ========================================================================

    void clearPlacementHighlights() {
        for (Hex hex : availablePlacementHexes) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) tile.setHighlighted(false);
        }
        availablePlacementHexes.clear();
        for (Hex hex : availableDistrictPlacementHexes) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) tile.setHighlighted(false);
        }
        availableDistrictPlacementHexes.clear();
    }
}
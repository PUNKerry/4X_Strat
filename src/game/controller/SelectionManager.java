package game.controller;

import game.model.unit.Unit;
import game.model.city.City;
import game.model.world.Hex;
import game.model.world.Tile;
import game.model.unit.Settler;

import java.util.Map;

/**
 * Управление выбором юнитов и городов, подсветкой доступных ходов.
 */
public class SelectionManager {

    private final GameController controller;

    private Unit selectedUnit = null;
    private City selectedCity = null;
    private Map<Hex, Integer> reachableHexes = null;

    public SelectionManager(GameController controller) {
        this.controller = controller;
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public Unit getSelectedUnit() { return selectedUnit; }
    public City getSelectedCity() { return selectedCity; }
    public Map<Hex, Integer> getReachableHexes() { return reachableHexes; }

    // ========================================================================
    // Выбор юнита
    // ========================================================================

    public void selectUnit(Unit unit) {
        if (unit == null) {
            selectedUnit = null;
            if (controller.onUnitSelected != null) controller.onUnitSelected.run();
            return;
        }
        selectedUnit = unit;
        highlightAvailableMoves(unit);
        if (controller.onUnitSelected != null) controller.onUnitSelected.run();

        if (controller.getAdvisor() != null && unit instanceof Settler &&
                !controller.getAdvisor().isSettlerTutorialShown()) {
            controller.getAdvisor().showSettlerAndWaterTutorial();
        }
    }

    // ========================================================================
    // Выбор города
    // ========================================================================

    public void selectCity(City city) {
        if (city == null) {
            selectedCity = null;
            controller.exitCityView();
            if (controller.onCitySelected != null) controller.onCitySelected.run();
            return;
        }
        selectedCity = city;
        controller.enterCityView(city);
        if (controller.onCitySelected != null) controller.onCitySelected.run();
    }

    // ========================================================================
    // Подсветка доступных ходов
    // ========================================================================

    public void highlightAvailableMoves(Unit unit) {
        clearHighlights();
        reachableHexes = unit.getReachableHexes(controller.getWorld(), controller.getHexGrid());
        for (Hex hex : reachableHexes.keySet()) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) tile.setHighlighted(true);
        }
    }

    // ========================================================================
    // Очистка выделений и подсветок
    // ========================================================================

    public void clearHighlights() {
        if (reachableHexes != null) {
            for (Hex hex : reachableHexes.keySet()) {
                Tile tile = controller.findTileAtHex(hex);
                if (tile != null) tile.setHighlighted(false);
            }
            reachableHexes = null;
        }
        // Также снимаем подсветку в режимах размещения (если они активны)
        if (controller.isPlacementMode()) {
            // Подсветка улучшений снимается в exitPlacementMode, но для надёжности:
            controller.exitPlacementMode();
        }
        if (controller.isDistrictPlacementMode()) {
            controller.exitDistrictPlacementMode();
        }
    }

    // ========================================================================
    // Полный сброс выбора (для новой игры)
    // ========================================================================

    public void clearSelection() {
        selectedUnit = null;
        selectedCity = null;
        clearHighlights();
    }
}
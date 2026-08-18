package game.controller;

import game.model.unit.*;
import game.model.city.City;
import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import game.model.world.World;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Управление юнитами: создание, спавн, перемещение, отдых, роспуск.
 */
public class UnitManager {

    private final GameController controller;
    private final TechTree techTree;
    private World world;
    private HexGrid hexGrid;

    private List<Unit> allUnits = new ArrayList<>();
    private Unit playerUnit = null;

    public UnitManager(GameController controller, TechTree techTree) {
        this.controller = controller;
        this.techTree = techTree;
        this.world = controller.getWorld();
        this.hexGrid = controller.getHexGrid();
    }

    /**
     * Обновляет ссылки на World и HexGrid при старте новой игры.
     */
    public void setWorldAndHexGrid(World world, HexGrid hexGrid) {
        this.world = world;
        this.hexGrid = hexGrid;
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public List<Unit> getAllUnits() {
        return allUnits;
    }

    public Unit getPlayerUnit() {
        return playerUnit;
    }

    public void setPlayerUnit(Unit unit) {
        this.playerUnit = unit;
    }

    // ========================================================================
    // Создание и спавн юнитов
    // ========================================================================

    public void spawnUnitNearCity(City city, String unitType) {
        Hex center = city.getCenter();
        for (Hex neighbor : center.neighbors()) {
            if (isHexFree(neighbor)) {
                Unit unit = createUnit(neighbor, unitType);
                if (unit != null) {
                    unit.setHomeCity(city);
                    world.addObject(unit);
                    allUnits.add(unit);
                    controller.recalculateFog();
                    return;
                }
            }
        }
        for (int dr = -2; dr <= 2; dr++) {
            for (int dc = -2; dc <= 2; dc++) {
                Hex h = new Hex(center.col + dc, center.row + dr);
                if (center.distanceTo(h) <= 2 && isHexFree(h)) {
                    Unit unit = createUnit(h, unitType);
                    if (unit != null) {
                        unit.setHomeCity(city);
                        world.addObject(unit);
                        allUnits.add(unit);
                        controller.recalculateFog();
                        return;
                    }
                }
            }
        }
        System.out.println("Не удалось разместить юнит " + unitType + " рядом с городом " + city.getName());
    }

    private Unit createUnit(Hex hex, String type) {
        switch (type) {
            case "warrior": return new Warrior(hex, hexGrid, techTree);
            case "archer": return new Archer(hex, hexGrid, techTree);
            case "chariot": return new Chariot(hex, hexGrid, techTree);
            case "bronze_swordsman": return new BronzeSwordsman(hex, hexGrid, techTree);
            case "horseman": return new Horseman(hex, hexGrid, techTree);
            case "galley": return new Galley(hex, hexGrid, techTree);
            case "battering_ram": return new BatteringRam(hex, hexGrid, techTree);
            default: return null;
        }
    }

    private boolean isHexFree(Hex hex) {
        for (Unit unit : allUnits) {
            if (unit.getCurrentHex() != null && unit.getCurrentHex().equals(hex)) {
                return false;
            }
        }
        return true;
    }

    public void spawnSettlerNearCity(City city) {
        Hex center = city.getCenter();
        for (Hex neighbor : center.neighbors()) {
            if (isHexFree(neighbor)) {
                createSettlerAt(neighbor, city);
                return;
            }
        }
        if (isHexFree(center)) {
            createSettlerAt(center, city);
        } else {
            System.out.println("Не удалось разместить поселенца: нет свободных клеток вокруг " + city.getName());
        }
    }

    public void spawnScoutNearCity(City city) {
        Hex center = city.getCenter();
        for (Hex neighbor : center.neighbors()) {
            if (isHexFree(neighbor)) {
                createScoutAt(neighbor);
                return;
            }
        }
        for (int dr = -2; dr <= 2; dr++) {
            for (int dc = -2; dc <= 2; dc++) {
                Hex h = new Hex(center.col + dc, center.row + dr);
                if (center.distanceTo(h) <= 2 && isHexFree(h)) {
                    createScoutAt(h);
                    return;
                }
            }
        }
        System.out.println("Не удалось разместить скаута рядом с городом " + city.getName());
    }

    private void createSettlerAt(Hex hex, City city) {
        Settler settler = new Settler(hex, hexGrid, techTree);
        settler.setHomeCity(city);
        world.addObject(settler);
        allUnits.add(settler);
        controller.recalculateFog();
        if (controller.onUnitSelected != null) Platform.runLater(controller.onUnitSelected);
        if (controller.onStatusChanged != null) Platform.runLater(controller.onStatusChanged);
    }

    private void createScoutAt(Hex hex) {
        Scout scout = new Scout(hex, hexGrid, techTree);
        if (!controller.getCities().isEmpty()) {
            scout.setHomeCity(controller.getCities().get(0));
        }
        world.addObject(scout);
        allUnits.add(scout);
        controller.recalculateFog();
        if (controller.onUnitSelected != null) Platform.runLater(controller.onUnitSelected);
        if (controller.onStatusChanged != null) Platform.runLater(controller.onStatusChanged);
    }

    // ========================================================================
    // Действия с юнитами
    // ========================================================================

    public void moveUnit(Unit unit, Hex targetHex) {
        if (unit == null) return;
        if (!unit.canMove()) return;
        Map<Hex, Integer> reachable = unit.getReachableHexes(world, hexGrid);
        if (!reachable.containsKey(targetHex)) return;
        int cost = reachable.get(targetHex);
        if (cost > unit.getMovementPoints()) return;
        if (unit.moveTo(targetHex, world, hexGrid)) {
            controller.clearHighlights();
            controller.recalculateFog();
            if (controller.onUnitSelected != null) controller.onUnitSelected.run();
            controller.updateUI();
        }
    }

    public void restUnit(Unit unit) {
        if (unit == null) return;
        unit.setResting(!unit.isResting());
        if (controller.onUnitSelected != null) controller.onUnitSelected.run();
        if (controller.onStatusChanged != null) controller.onStatusChanged.run();
    }

    public void disbandUnit(Unit unit) {
        if (unit == null) return;
        world.removeObject(unit);
        allUnits.remove(unit);
        if (unit == playerUnit) playerUnit = null;
        controller.clearHighlights();
        controller.selectUnit(null);
        if (controller.onUnitSelected != null) controller.onUnitSelected.run();
        if (controller.onStatusChanged != null) controller.onStatusChanged.run();
        controller.recalcIncome();
        controller.recalculateFog();
    }

    // ========================================================================
    // Обновление юнитов в конце хода (используется в TurnManager)
    // ========================================================================

    public void updateUnitsEndTurn() {
        List<Unit> toRemove = new ArrayList<>();
        for (Unit unit : allUnits) {
            unit.checkFatigueAndCasualties();
            if (unit.getPopulation() == 0 && unit.getSquadMembers() == 0) {
                toRemove.add(unit);
            }
        }
        for (Unit unit : toRemove) {
            world.removeObject(unit);
            allUnits.remove(unit);
            if (unit == playerUnit) playerUnit = null;
            if (controller.getSelectedUnit() == unit) {
                controller.clearHighlights();
                controller.selectUnit(null);
            }
        }

        // Сброс очков движения и действия
        for (Unit unit : allUnits) {
            unit.resetMovementPoints();
            unit.resetActionPoints();
        }
    }

    // ========================================================================
    // Сброс для новой игры
    // ========================================================================

    public void reset() {
        allUnits.clear();
        playerUnit = null;
    }
}
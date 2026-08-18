package game.controller;


import game.model.city.City;
import game.model.unit.Unit;
import game.model.world.FogOfWar;
import game.model.world.Hex;

import java.util.HashSet;
import java.util.Set;

/**
 * Управление туманом войны.
 * Отвечает за пересчёт видимости на основе юнитов и городов игрока.
 */
public class FogManager {

    private final GameController controller;
    private final FogOfWar fogOfWar;

    public FogManager(GameController controller, FogOfWar fogOfWar) {
        this.controller = controller;
        this.fogOfWar = fogOfWar;
        // Больше не присваиваем StaticComponents.fogOfWar
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public FogOfWar getFogOfWar() {
        return fogOfWar;
    }

    // ========================================================================
    // Пересчёт тумана войны
    // ========================================================================

    public void recalculateFog() {
        int cols = controller.getCols();
        int rows = controller.getRows();
        Set<Hex> visibleHexes = new HashSet<>();

        // Видимость от юнитов
        for (Unit unit : controller.getAllUnits()) {
            Hex center = unit.getCurrentHex();
            if (center == null) continue;
            int radius = unit.getSightRadius();
            for (int dr = -radius; dr <= radius; dr++) {
                for (int dc = -radius; dc <= radius; dc++) {
                    Hex h = new Hex(center.col + dc, center.row + dr);
                    if (center.distanceTo(h) <= radius && h.col >= 0 && h.col < cols && h.row >= 0 && h.row < rows) {
                        visibleHexes.add(h);
                    }
                }
            }
        }

        // Видимость от городов (радиус 2)
        for (City city : controller.getCities()) {
            int radius = 2;
            for (Hex hex : city.getTiles()) {
                for (int dr = -radius; dr <= radius; dr++) {
                    for (int dc = -radius; dc <= radius; dc++) {
                        Hex h = new Hex(hex.col + dc, hex.row + dr);
                        if (hex.distanceTo(h) <= radius && h.col >= 0 && h.col < cols && h.row >= 0 && h.row < rows) {
                            visibleHexes.add(h);
                        }
                    }
                }
            }
        }

        fogOfWar.updateVisibility(visibleHexes);
    }

    // ========================================================================
    // Сброс тумана (новая игра)
    // ========================================================================

    public void reset() {
        fogOfWar.reset();
    }
}
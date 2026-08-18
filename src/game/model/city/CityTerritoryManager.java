package game.model.city;

import engine.core.GameObject;
import game.controller.GameController;
import game.model.world.Hex;
import game.model.world.TerrainType;
import game.model.world.Tile;
import game.model.world.World;

import java.util.*;

/**
 * Управление территорией города: клетки, расширение границ, пресная вода.
 */
public class CityTerritoryManager {

    private final City city;
    private final World world;
    private final GameController controller;
    private final Hex center;
    private Set<Hex> tiles = new HashSet<>();
    private int expansionTimer = 0;
    private static final int EXPANSION_THRESHOLD = 10;
    private boolean hasFreshWater = false;
    private static final int FRESH_WATER_RADIUS = 3;

    public CityTerritoryManager(City city, World world, GameController controller, Hex center) {
        this.city = city;
        this.world = world;
        this.controller = controller;
        this.center = center;
        // Инициализация территории: центр + соседи
        tiles.add(center);
        for (Hex neighbor : center.neighbors()) {
            tiles.add(neighbor);
        }
        checkFreshWater();
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public Set<Hex> getTiles() {
        return tiles;
    }

    public int getExpansionTimer() {
        return expansionTimer;
    }

    public boolean hasFreshWater() {
        return hasFreshWater;
    }

    // ========================================================================
    // Проверка пресной воды
    // ========================================================================

    public void checkFreshWater() {
        hasFreshWater = false;
        for (int dr = -FRESH_WATER_RADIUS; dr <= FRESH_WATER_RADIUS; dr++) {
            for (int dc = -FRESH_WATER_RADIUS; dc <= FRESH_WATER_RADIUS; dc++) {
                Hex h = new Hex(center.col + dc, center.row + dr);
                if (center.distanceTo(h) <= FRESH_WATER_RADIUS) {
                    Tile tile = findTile(world, h);
                    if (tile != null && tile.getTerrain() == TerrainType.RIVER) {
                        hasFreshWater = true;
                        return;
                    }
                }
            }
        }
    }

    // ========================================================================
    // Расширение границ
    // ========================================================================

    public void updateExpansion(int happiness, int population, int housingCapacity, boolean isStarving) {
        boolean conditionsMet = (happiness > 50 && population <= housingCapacity && !isStarving);
        if (conditionsMet) {
            expansionTimer++;
            if (expansionTimer >= EXPANSION_THRESHOLD) {
                expandBorder();
                expansionTimer = 0;
            }
        } else {
            expansionTimer = 0;
        }
    }

    private void expandBorder() {
        Set<Hex> neighbors = new HashSet<>();
        for (Hex hex : tiles) {
            for (Hex neighbor : hex.neighbors()) {
                if (!tiles.contains(neighbor) && !neighbors.contains(neighbor)) {
                    if (controller != null && controller.findCityAtHex(neighbor) == null) {
                        if (neighbor.col >= 0 && neighbor.col < controller.getCols() &&
                                neighbor.row >= 0 && neighbor.row < controller.getRows()) {
                            neighbors.add(neighbor);
                        }
                    }
                }
            }
        }
        if (neighbors.isEmpty()) return;
        Random rand = new Random();
        List<Hex> list = new ArrayList<>(neighbors);
        Hex newTile = list.get(rand.nextInt(list.size()));
        tiles.add(newTile);
        Tile tile = controller.findTileAtHex(newTile);
        if (tile != null) {
            Set<Hex> cityTiles = new HashSet<>(tiles);
            tile.setCityTiles(cityTiles);
            if (!tile.isCityTile()) {
                tile.setCityTile(true);
            }
        }
        checkFreshWater();
    }

    // ========================================================================
    // Вспомогательный метод для поиска тайла
    // ========================================================================

    private Tile findTile(World world, Hex hex) {
        for (GameObject obj : world.getAllObjects()) {
            if (obj instanceof Tile) {
                Tile tile = (Tile) obj;
                if (tile.getHex().equals(hex)) return tile;
            }
        }
        return null;
    }

    // ========================================================================
    // Сброс для новой игры
    // ========================================================================

    public void reset(Hex newCenter) {
        tiles.clear();
        tiles.add(newCenter);
        for (Hex neighbor : newCenter.neighbors()) {
            tiles.add(neighbor);
        }
        expansionTimer = 0;
        checkFreshWater();
    }
}
package game.model.world;

import engine.core.GameObject;
import engine.graphics.SpriteManager;

import java.util.Random;

public class WorldGenerator {

    private final World world;
    private final HexGrid hexGrid;
    private final int cols;
    private final int rows;
    private final Random random;
    private final SpriteManager spriteManager;

    public WorldGenerator(World world, HexGrid hexGrid, int cols, int rows) {
        this.world = world;
        this.hexGrid = hexGrid;
        this.cols = cols;
        this.rows = rows;
        this.random = new Random(42);
        this.spriteManager = SpriteManager.getInstance();
    }

    public Hex generate() {
        TerrainType[][] map = generateBiomeMap();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Hex hex = new Hex(c, r);
                TerrainType terrain = map[r][c];
                var sprite = spriteManager.getSprite(terrain.getSpritePath());
                Tile tile = new Tile(hex, terrain, sprite);
                world.addObject(tile);
            }
        }
        return findLandHex(30, 20);
    }

    private TerrainType[][] generateBiomeMap() {
        TerrainType[][] map = new TerrainType[rows][cols];
        // Инициализация равниной
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                map[r][c] = TerrainType.PLAIN;
            }
        }

        // Снег на севере и юге
        int snowNorth = 8, snowSouth = rows - 8;
        for (int r = 0; r < snowNorth; r++) {
            for (int c = 0; c < cols; c++) {
                if (random.nextDouble() < 0.6) map[r][c] = TerrainType.SNOW;
            }
        }
        for (int r = snowSouth; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (random.nextDouble() < 0.6) map[r][c] = TerrainType.SNOW;
            }
        }

        // Лесные кластеры
        int forestClusters = 25;
        for (int i = 0; i < forestClusters; i++) {
            int cr = 5 + random.nextInt(rows - 10);
            int cc = 5 + random.nextInt(cols - 10);
            spreadBiome(map, cr, cc, TerrainType.FOREST, 0.45, 3 + random.nextInt(4));
        }

        // Пустыни
        int desertClusters = 15;
        for (int i = 0; i < desertClusters; i++) {
            int cr = 10 + random.nextInt(rows - 20);
            int cc = 10 + random.nextInt(cols - 20);
            spreadBiome(map, cr, cc, TerrainType.DESERT, 0.4, 3 + random.nextInt(4));
        }

        // Океаны
        int oceanClusters = 8;
        for (int i = 0; i < oceanClusters; i++) {
            int cr = 5 + random.nextInt(rows - 10);
            int cc = 5 + random.nextInt(cols - 10);
            spreadBiome(map, cr, cc, TerrainType.OCEAN, 0.6, 5 + random.nextInt(6));
        }

        // Тропики
        int tropicalClusters = 10;
        for (int i = 0; i < tropicalClusters; i++) {
            int cr = 12 + random.nextInt(rows - 24);
            int cc = 12 + random.nextInt(cols - 24);
            spreadBiome(map, cr, cc, TerrainType.TROPICAL, 0.5, 2 + random.nextInt(3));
        }

        // Джунгли
        int jungleClusters = 8;
        for (int i = 0; i < jungleClusters; i++) {
            int cr = 15 + random.nextInt(rows - 30);
            int cc = 15 + random.nextInt(cols - 30);
            spreadBiome(map, cr, cc, TerrainType.JUNGLE, 0.4, 3 + random.nextInt(4));
        }

        // Холмы и горы
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (map[r][c] == TerrainType.PLAIN && random.nextDouble() < 0.05) {
                    map[r][c] = TerrainType.HILL;
                }
                if (map[r][c] == TerrainType.PLAIN && random.nextDouble() < 0.02) {
                    map[r][c] = TerrainType.MOUNTAIN;
                }
            }
        }

        // Реки – теперь их не слишком много, и они не перекрывают всю карту
        for (int i = 0; i < 4; i++) {
            int startR = 10 + random.nextInt(rows - 20);
            int startC = 10 + random.nextInt(cols - 20);
            drawRiver(map, startR, startC, 6 + random.nextInt(8));
        }

        return map;
    }

    private void spreadBiome(TerrainType[][] map, int row, int col, TerrainType type, double prob, int radius) {
        for (int dr = -radius; dr <= radius; dr++) {
            for (int dc = -radius; dc <= radius; dc++) {
                int nr = row + dr;
                int nc = col + dc;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                double dist = Math.sqrt(dr * dr + dc * dc);
                if (dist > radius) continue;
                if (map[nr][nc] == TerrainType.PLAIN && random.nextDouble() < prob * (1 - dist / radius)) {
                    map[nr][nc] = type;
                }
            }
        }
    }

    private void drawRiver(TerrainType[][] map, int startR, int startC, int length) {
        int r = startR, c = startC;
        for (int i = 0; i < length; i++) {
            if (r < 0 || r >= rows || c < 0 || c >= cols) break;
            // Река не заливает океан и не превращает всё в реку
            if (map[r][c] != TerrainType.OCEAN && map[r][c] != TerrainType.RIVER) {
                map[r][c] = TerrainType.RIVER;
            }
            int dir = random.nextInt(6);
            Hex hex = new Hex(c, r);
            Hex[] neighbors = hex.neighbors();
            if (dir < neighbors.length) {
                r = neighbors[dir].row;
                c = neighbors[dir].col;
            }
        }
    }

    private Hex findLandHex(int startCol, int startRow) {
        int radius = 5;
        for (int r = startRow - radius; r <= startRow + radius; r++) {
            for (int c = startCol - radius; c <= startCol + radius; c++) {
                if (c < 0 || c >= cols || r < 0 || r >= rows) continue;
                Hex hex = new Hex(c, r);
                Tile tile = findTileAtHex(hex);
                if (tile != null && tile.getTerrain() != TerrainType.OCEAN) {
                    return hex;
                }
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Hex hex = new Hex(c, r);
                Tile tile = findTileAtHex(hex);
                if (tile != null && tile.getTerrain() != TerrainType.OCEAN) {
                    return hex;
                }
            }
        }
        return new Hex(0, 0);
    }

    private Tile findTileAtHex(Hex hex) {
        for (GameObject obj : world.getAllObjects()) {
            if (obj instanceof Tile) {
                Tile tile = (Tile) obj;
                if (tile.getHex().equals(hex)) return tile;
            }
        }
        return null;
    }
}
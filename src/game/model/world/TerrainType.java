package game.model.world;

import javafx.scene.paint.Color;

public enum TerrainType {
    PLAIN("Равнина", Color.rgb(200, 230, 201), 1, 2, 1, false, "resources/sprites/terrain/plain.png"),
    HILL("Холм", Color.rgb(215, 204, 200), 3, 1, 2, false, "resources/sprites/terrain/hill.png"),
    RIVER("Река", Color.rgb(144, 202, 249), Integer.MAX_VALUE, 3, 1, true, "resources/sprites/terrain/river.png"),
    MOUNTAIN("Гора", Color.rgb(176, 190, 197), -1, 0, 3, false, "resources/sprites/terrain/mountain.png"),
    FOREST("Лес", Color.rgb(165, 214, 167), 2, 1, 1, false, "resources/sprites/terrain/forest.png"),
    DESERT("Пустыня", Color.rgb(255, 224, 178), 1, 0, 0, false, "resources/sprites/terrain/desert.png"),
    SNOW("Снег", Color.rgb(232, 234, 246), 1, 0, 0, false, "resources/sprites/terrain/snow.png"),
    OCEAN("Море", Color.rgb(129, 212, 250), -1, 1, 0, false, "resources/sprites/terrain/ocean.png"),
    TROPICAL("Тропики", Color.rgb(200, 230, 201), 1, 2, 1, false, "resources/sprites/terrain/tropical.png"),
    JUNGLE("Джунгли", Color.rgb(165, 214, 167), 2, 1, 1, false, "resources/sprites/terrain/jungle.png");

    private final String name;
    private final Color color;
    private final int movementCost;
    private final int food;
    private final int production;
    private final boolean isRiver;
    private final String spritePath;

    TerrainType(String name, Color color, int movementCost, int food, int production, boolean isRiver, String spritePath) {
        this.name = name;
        this.color = color;
        this.movementCost = movementCost;
        this.food = food;
        this.production = production;
        this.isRiver = isRiver;
        this.spritePath = spritePath;
    }

    public String getName() { return name; }
    public Color getColor() { return color; }
    public int getMovementCost() { return movementCost; }
    public int getFood() { return food; }
    public int getProduction() { return production; }
    public boolean isPassable() { return movementCost != -1; }
    public boolean isRiver() { return isRiver; }
    public String getSpritePath() { return spritePath; }

    // game.model.world.TerrainType
    public boolean isWater() {
        return this == OCEAN || this == RIVER;
    }
}
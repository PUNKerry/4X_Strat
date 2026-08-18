package game.model.registry;

import game.model.world.Improvement;
import game.model.world.TerrainType;

import java.util.Set;

/**
 * Данные о клеточном улучшении (ферма, рудник и т.д.)
 * Используется для конфигурации и проверок.
 */
public class ImprovementData {
    private final Improvement.Type type;
    private final String displayName;
    private final int cost;
    private final String requiredTech;
    private final int foodBonus;
    private final int productionBonus;
    private final Set<TerrainType> allowedTerrain;
    private final int workersForConstruction;
    private final int workersToOperate;

    public ImprovementData(Improvement.Type type, String displayName, int cost, String requiredTech,
                           int foodBonus, int productionBonus, Set<TerrainType> allowedTerrain,
                           int workersForConstruction, int workersToOperate) {
        this.type = type;
        this.displayName = displayName;
        this.cost = cost;
        this.requiredTech = requiredTech;
        this.foodBonus = foodBonus;
        this.productionBonus = productionBonus;
        this.allowedTerrain = allowedTerrain;
        this.workersForConstruction = workersForConstruction;
        this.workersToOperate = workersToOperate;
    }

    public Improvement.Type getType() { return type; }
    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public String getRequiredTech() { return requiredTech; }
    public int getFoodBonus() { return foodBonus; }
    public int getProductionBonus() { return productionBonus; }
    public Set<TerrainType> getAllowedTerrain() { return allowedTerrain; }
    public int getWorkersForConstruction() { return workersForConstruction; }
    public int getWorkersToOperate() { return workersToOperate; }

    public boolean isAllowedOnTerrain(TerrainType terrain) {
        return allowedTerrain.contains(terrain);
    }

    public boolean isTechAvailable(TechRegistry techRegistry) {
        return requiredTech == null || requiredTech.isEmpty() || techRegistry.isResearched(requiredTech);
    }
}
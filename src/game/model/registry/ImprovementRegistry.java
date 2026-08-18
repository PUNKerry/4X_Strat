package game.model.registry;

import game.model.world.Improvement;
import game.model.world.TerrainType;

import java.util.*;

public class ImprovementRegistry {
    private final Map<Improvement.Type, ImprovementData> map = new HashMap<>();

    // game.model.registry.ImprovementRegistry
    public ImprovementRegistry() {
        register(new ImprovementData(
                Improvement.Type.FARM, "Ферма", 30, "Земледелие (мотыжное)",
                2, 0, Set.of(TerrainType.PLAIN, TerrainType.RIVER, TerrainType.TROPICAL, TerrainType.DESERT), 4, 4
        ));
        register(new ImprovementData(
                Improvement.Type.MINE, "Рудник", 40, "Металлургия меди",
                0, 2, Set.of(TerrainType.HILL, TerrainType.MOUNTAIN), 5, 1
        ));
        register(new ImprovementData(
                Improvement.Type.PASTURE, "Пастбище", 25, "Скотоводство",
                1, 1, Set.of(TerrainType.PLAIN, TerrainType.TROPICAL), 3, 2
        ));
        register(new ImprovementData(
                Improvement.Type.LUMBERMILL, "Лесопилка", 30, "Примитивное плотничество",
                0, 2, Set.of(TerrainType.FOREST, TerrainType.JUNGLE), 4, 1
        ));
        register(new ImprovementData(
                Improvement.Type.QUARRY, "Каменоломня", 35, "Строительство дорог",
                0, 1, Set.of(TerrainType.HILL, TerrainType.MOUNTAIN), 5, 2
        ));
    }

    private void register(ImprovementData data) {
        map.put(data.getType(), data);
    }

    public ImprovementData get(Improvement.Type type) {
        return map.get(type);
    }

    public List<ImprovementData> getAll() {
        return new ArrayList<>(map.values());
    }

    public boolean isTechAvailable(Improvement.Type type, TechRegistry techRegistry) {
        ImprovementData data = get(type);
        return data != null && data.isTechAvailable(techRegistry);
    }
}
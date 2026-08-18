package game.model.registry;

import game.model.world.Improvement;
import game.model.world.TerrainType;

import java.util.*;

public class ImprovementRegistry {
    private final Map<Improvement.Type, ImprovementData> map = new HashMap<>();

    public ImprovementRegistry() {
        // Регистрируем все улучшения
        register(new ImprovementData(
                Improvement.Type.FARM,
                "Ферма",
                30,
                "Земледелие (мотыжное)",
                2,
                0,
                Set.of(TerrainType.PLAIN, TerrainType.RIVER, TerrainType.TROPICAL, TerrainType.DESERT),
                4,
                4
        ));

        register(new ImprovementData(
                Improvement.Type.MINE,
                "Рудник",
                40,
                "Металлургия меди",
                0,
                2,
                Set.of(TerrainType.HILL, TerrainType.MOUNTAIN),
                5,
                1
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
package game.model.registry;

import game.model.city.District;

import java.util.*;

public class DistrictRegistry {
    private final Map<District.Type, DistrictData> map = new HashMap<>();

    public DistrictRegistry() {
        register(new DistrictData(
                District.Type.HOUSING,
                "Жилища",
                50,
                "Примитивное плотничество",
                0, 0, 500, 0,
                4
        ));
        register(new DistrictData(
                District.Type.DISTRICT_1,
                "Район науки",
                60,
                null, // всегда доступен
                1, 0, 0, 0,
                4
        ));
        register(new DistrictData(
                District.Type.DISTRICT_2,
                "Район культуры",
                60,
                null,
                0, 1, 0, 0,
                4
        ));
    }

    private void register(DistrictData data) {
        map.put(data.getType(), data);
    }

    public DistrictData get(District.Type type) {
        return map.get(type);
    }

    public List<DistrictData> getAll() {
        return new ArrayList<>(map.values());
    }

    public boolean isTechAvailable(District.Type type, TechRegistry techRegistry) {
        DistrictData data = get(type);
        return data != null && data.isTechAvailable(techRegistry);
    }
}
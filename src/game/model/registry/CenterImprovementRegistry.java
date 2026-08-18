package game.model.registry;

import java.util.*;

public class CenterImprovementRegistry {
    private final Map<String, CenterImprovementData> map = new LinkedHashMap<>();

    public CenterImprovementRegistry() {
        register(new CenterImprovementData(
                "Дом собраний", "Огонь",
                1, 0, 1, 0, 0, 0, 0, 0,
                "Дом собраний\n+1 еда, +1 наука\nТребуется: Огонь"
        ));
        register(new CenterImprovementData(
                "Амбар", "Гончарство",
                2, 0, 0, 0, 0, 0, 0, 0,
                "Амбар\n+2 еды\nТребуется: Гончарство"
        ));
        register(new CenterImprovementData(
                "Склад с водой", "Гончарство",
                0, 2, 0, 0, 0, 0, 0, 0,
                "Склад с водой\n+2 производства\nТребуется: Гончарство"
        ));
        register(new CenterImprovementData(
                "Частокол", "Примитивное плотничество",
                0, 0, 0, 0, 0, 0, 0, 0,
                "Частокол\nЗащита (пока не реализована)\nТребуется: Примитивное плотничество"
        ));
        register(new CenterImprovementData(
                "Библиотека", "Письменность",
                0, 0, 2, 0, 0, 0, 0, 0,
                "Библиотека\n+2 науки\nТребуется: Письменность"
        ));
        register(new CenterImprovementData(
                "Амфитеатр", "Искусство",
                0, 0, 0, 2, 0, 0, 0, 0,
                "Амфитеатр\n+2 культуры\nТребуется: Искусство"
        ));
        register(new CenterImprovementData(
                "Театр", "Театр",
                0, 0, 0, 2, 0, 0, 1, 0,
                "Театр\n+2 культуры, +1 счастье\nТребуется: Театр (культурная тех.)"
        ));
        register(new CenterImprovementData(
                "Суд", "Право",
                0, 0, 0, 0, 0, 0, 2, 0,
                "Суд\n+2 счастья, снижение коррупции\nТребуется: Право"
        ));
        register(new CenterImprovementData(
                "Совет старейшин", "Вождество",
                0, 0, 0, 1, 0, 0, 0, 1,
                "Совет старейшин\n+1 культура, +1 легитимность\nТребуется: Вождество"
        ));
        register(new CenterImprovementData(
                "Народное собрание", "Город-государство",
                0, 0, 0, 1, 0, 0, 1, 0,
                "Народное собрание\n+1 культура, +1 счастье\nТребуется: Город-государство"
        ));
        register(new CenterImprovementData(
                "Сенат", "Республика",
                0, 0, 0, 2, 0, 0, 0, 1,
                "Сенат\n+2 культуры, +1 легитимность\nТребуется: Республика"
        ));
        register(new CenterImprovementData(
                "Аристократический совет", "Олигархия",
                0, 2, 0, 1, 0, 0, 0, 0,
                "Аристократический совет\n+2 производства, +1 культура\nТребуется: Олигархия"
        ));
        register(new CenterImprovementData(
                "Королевский двор", "Монархия",
                0, 0, 0, 1, 0, 0, 0, 2,
                "Королевский двор\n+2 легитимности, +1 культура\nТребуется: Монархия"
        ));
        register(new CenterImprovementData(
                "Акведук", "Математика",
                1, 0, 0, 0, 0, 2, 0, 0,
                "Акведук\n+2 жилья, +1 еда\nТребуется: Математика"
        ));
        register(new CenterImprovementData(
                "Обсерватория", "Астрономия",
                0, 0, 2, 0, 0, 0, 0, 0,
                "Обсерватория\n+2 науки\nТребуется: Астрономия"
        ));
        register(new CenterImprovementData(
                "Храм", "Политеизм",
                0, 0, 0, 1, 2, 0, 0, 0,
                "Храм\n+2 веры, +1 культура\nТребуется: Политеизм"
        ));
        register(new CenterImprovementData(
                "Оракулы", "Оракулы",
                0, 0, 1, 0, 1, 0, 0, 0,
                "Оракулы\n+1 наука, +1 вера\nТребуется: Оракулы (религия)"
        ));
        register(new CenterImprovementData(
                "Монастыри", "Пророчества",
                0, 0, 2, 0, 2, 0, 0, 0,
                "Монастыри\n+2 веры, +2 науки\nТребуется: Пророчества"
        ));
    }

    private void register(CenterImprovementData data) {
        map.put(data.getName(), data);
    }

    public CenterImprovementData get(String name) {
        return map.get(name);
    }

    public List<CenterImprovementData> getAll() {
        return new ArrayList<>(map.values());
    }

    public List<CenterImprovementData> getAvailable(TechRegistry techRegistry) {
        List<CenterImprovementData> list = new ArrayList<>();
        for (CenterImprovementData data : map.values()) {
            if (data.isTechAvailable(techRegistry)) {
                list.add(data);
            }
        }
        return list;
    }
}
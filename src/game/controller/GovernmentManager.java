package game.controller;

import game.model.government.Law;
import game.model.registry.TechRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class GovernmentManager {

    private final GameController controller;
    private final TechRegistry techRegistry;
    private String currentGovernment;
    private final List<Law> activeLaws;
    private final List<Law> allLaws;
    private final Map<Law, Integer> repealedLaws; // закон -> ходов до восстановления

    public GovernmentManager(GameController controller, TechRegistry techRegistry) {
        this.controller = controller;
        this.techRegistry = techRegistry;
        this.currentGovernment = "Вождество"; // теперь стартовая форма
        this.activeLaws = new ArrayList<>();
        this.allLaws = new ArrayList<>();
        this.repealedLaws = new HashMap<>();
        initLaws();
    }

    private void initLaws() {
        // Законы, доступные с определённых технологий или форм правления
        allLaws.add(new Law("Общинные собрания", "Укрепляет общину", "Традиции", null,
                0, 1, 0, 1, 1, 0));
        allLaws.add(new Law("Кодекс законов", "Систематизация законодательства", "Кодекс законов", null,
                0, 2, 0, 2, 2, 0));
        allLaws.add(new Law("Народное собрание", "Граждане участвуют в управлении", "Город-государство", "Республика",
                0, 2, 0, 2, 1, 0));
        allLaws.add(new Law("Сенат", "Совет старейшин принимает законы", "Республика", "Республика",
                0, 3, 0, 1, 2, 0));
        allLaws.add(new Law("Аристократический совет", "Знать управляет государством", "Олигархия", "Олигархия",
                0, 1, 3, 0, 2, 0));
        allLaws.add(new Law("Королевский двор", "Монарх и его советники", "Монархия", "Монархия",
                0, 1, 2, 0, 3, 0));
        allLaws.add(new Law("Имперский эдикт", "Воля императора – закон", "Империя", "Империя",
                1, 1, 3, -1, 5, 0));
        allLaws.add(new Law("Религиозная терпимость", "Свобода вероисповедания", "Монотеизм", null,
                0, 2, 0, 2, 1, 3));
        allLaws.add(new Law("Десятина", "Церковный налог", "Епископат", null,
                0, 0, 0, -1, 0, 5));
        allLaws.add(new Law("Налог на торговлю", "Пошлина на товары", "Стекло", null,
                0, 0, 0, 0, 0, 0)); // пока без золота
    }

    public String getCurrentGovernment() {
        return currentGovernment;
    }

    public void setCurrentGovernment(String government) {
        this.currentGovernment = government;
    }

    /**
     * Обновляет таймеры отменённых законов (вызывается в конце хода).
     */
    public void updateRepealedLaws() {
        Iterator<Map.Entry<Law, Integer>> it = repealedLaws.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Law, Integer> entry = it.next();
            int turnsLeft = entry.getValue() - 1;
            if (turnsLeft <= 0) {
                it.remove();
            } else {
                entry.setValue(turnsLeft);
            }
        }
    }

    public List<Law> getAvailableLaws() {
        List<Law> available = new ArrayList<>();
        for (Law law : allLaws) {
            if (activeLaws.contains(law)) continue;
            if (repealedLaws.containsKey(law)) continue; // на перезарядке
            if (law.getRequiredTech() != null && !techRegistry.isResearched(law.getRequiredTech())) continue;
            if (law.getRequiredGovernment() != null && !law.getRequiredGovernment().equals(currentGovernment)) continue;
            available.add(law);
        }
        return available;
    }

    public List<Law> getActiveLaws() {
        return new ArrayList<>(activeLaws);
    }

    public List<Law> getTemporarilyUnavailableLaws() {
        List<Law> unavailable = new ArrayList<>();
        for (Law law : allLaws) {
            if (activeLaws.contains(law)) continue;
            // Проверяем, почему недоступен
            if (repealedLaws.containsKey(law)) {
                unavailable.add(law);
                continue;
            }
            if (law.getRequiredTech() != null && !techRegistry.isResearched(law.getRequiredTech())) {
                unavailable.add(law);
                continue;
            }
            if (law.getRequiredGovernment() != null && !law.getRequiredGovernment().equals(currentGovernment)) {
                unavailable.add(law);
                continue;
            }
        }
        return unavailable;
    }

    public boolean adoptLaw(Law law) {
        if (activeLaws.contains(law)) return false;
        if (repealedLaws.containsKey(law)) return false;
        if (law.getRequiredTech() != null && !techRegistry.isResearched(law.getRequiredTech())) return false;
        if (law.getRequiredGovernment() != null && !law.getRequiredGovernment().equals(currentGovernment)) return false;
        activeLaws.add(law);
        law.setActive(true);
        return true;
    }

    public boolean repealLaw(Law law) {
        if (!activeLaws.contains(law)) return false;
        activeLaws.remove(law);
        law.setActive(false);
        // Закон становится недоступным на 10 ходов
        repealedLaws.put(law, 10);
        return true;
    }

    public int getRepealCooldown(Law law) {
        return repealedLaws.getOrDefault(law, 0);
    }

    public int getTotalScienceBonus() {
        return activeLaws.stream().mapToInt(Law::getScienceBonus).sum();
    }

    public int getTotalCultureBonus() {
        return activeLaws.stream().mapToInt(Law::getCultureBonus).sum();
    }

    public int getTotalProductionBonus() {
        return activeLaws.stream().mapToInt(Law::getProductionBonus).sum();
    }

    public int getTotalHappinessBonus() {
        return activeLaws.stream().mapToInt(Law::getHappinessBonus).sum();
    }

    public int getTotalLegitimacyBonus() {
        return activeLaws.stream().mapToInt(Law::getLegitimacyBonus).sum();
    }

    public int getTotalFaithBonus() {
        return activeLaws.stream().mapToInt(Law::getFaithBonus).sum();
    }

    public void reset() {
        activeLaws.clear();
        repealedLaws.clear();
        currentGovernment = "Вождество";
        for (Law law : allLaws) {
            law.setActive(false);
        }
    }
}
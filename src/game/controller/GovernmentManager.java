package game.controller;

import game.model.government.Law;
import game.model.registry.TechRegistry;

import java.util.ArrayList;
import java.util.List;

public class GovernmentManager {

    private final GameController controller;
    private final TechRegistry techRegistry;
    private String currentGovernment;
    private final List<Law> activeLaws;
    private final List<Law> allLaws;

    public GovernmentManager(GameController controller, TechRegistry techRegistry) {
        this.controller = controller;
        this.techRegistry = techRegistry;
        this.currentGovernment = "Родоплеменной строй";
        this.activeLaws = new ArrayList<>();
        this.allLaws = new ArrayList<>();
        initLaws();
    }

    private void initLaws() {
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

    public List<Law> getAvailableLaws() {
        List<Law> available = new ArrayList<>();
        for (Law law : allLaws) {
            if (activeLaws.contains(law)) continue;
            if (law.getRequiredTech() != null && !techRegistry.isResearched(law.getRequiredTech())) continue;
            if (law.getRequiredGovernment() != null && !law.getRequiredGovernment().equals(currentGovernment)) continue;
            available.add(law);
        }
        return available;
    }

    public boolean adoptLaw(Law law) {
        if (activeLaws.contains(law)) return false;
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
        return true;
    }

    public List<Law> getActiveLaws() {
        return new ArrayList<>(activeLaws);
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
        currentGovernment = "Родоплеменной строй";
        for (Law law : allLaws) {
            law.setActive(false);
        }
    }
}
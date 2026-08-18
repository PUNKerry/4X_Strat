package game.controller;

import game.model.government.Law;
import game.model.registry.TechRegistry;

import java.util.*;

public class GovernmentManager {

    private final GameController controller;
    private final TechRegistry techRegistry;
    private String currentGovernment;
    private final List<Law> activeLaws;
    private final List<Law> allLaws;
    private final Map<Law, Integer> repealedLaws; // закон -> ходов до восстановления
    private final Map<String, Integer> legitimacyModifiers; // событие -> дельта

    // Политический процесс
    private Law pendingLaw = null;
    private int politicalProcessTurns = 0;
    private int politicalProcessMaxTurns = 5;
    private boolean politicalProcessActive = false;
    private String politicalProcessStatus = "";

    public GovernmentManager(GameController controller, TechRegistry techRegistry) {
        this.controller = controller;
        this.techRegistry = techRegistry;
        this.currentGovernment = "Вождество";
        this.activeLaws = new ArrayList<>();
        this.allLaws = new ArrayList<>();
        this.repealedLaws = new HashMap<>();
        this.legitimacyModifiers = new HashMap<>();
        initLaws();
    }

    private void initLaws() {
        allLaws.add(new Law("Общинные собрания",
                "Укрепляет общину, но отвлекает от работы",
                "Традиции", null,
                0, 1, -1, 1, 1, 0));

        allLaws.add(new Law("Кодекс законов",
                "Систематизация законодательства, но бюрократия замедляет решения",
                "Кодекс законов", null,
                0, 2, -1, 2, -1, 0));

        allLaws.add(new Law("Народное собрание",
                "Граждане участвуют в управлении, но это замедляет решения",
                "Город-государство", "Республика",
                0, 2, -1, 2, 1, 0));

        allLaws.add(new Law("Сенат",
                "Совет старейшин принимает законы, но аристократы требуют привилегий",
                "Республика", "Республика",
                0, 3, -1, 1, 2, -1));

        allLaws.add(new Law("Аристократический совет",
                "Знать управляет государством, но подавляет простых граждан",
                "Олигархия", "Олигархия",
                0, 1, 3, -1, 2, -1));

        allLaws.add(new Law("Королевский двор",
                "Монарх и его советники, но содержание двора дорого",
                "Монархия", "Монархия",
                0, 1, 2, -1, 3, -1));

        allLaws.add(new Law("Имперский эдикт",
                "Воля императора – закон, но народ страдает от произвола",
                "Империя", "Империя",
                1, 1, 3, -2, 5, -1));

        allLaws.add(new Law("Религиозная терпимость",
                "Свобода вероисповедания, но ослабляет единую идеологию",
                "Монотеизм", null,
                0, 2, 0, 2, -1, 3));

        allLaws.add(new Law("Десятина",
                "Церковный налог, но он бьёт по бедным",
                "Епископат", null,
                0, 0, 0, -2, 0, 5));

        allLaws.add(new Law("Налог на торговлю",
                "Пошлина на товары, но снижает деловую активность",
                "Стекло", null,
                0, 0, -1, 0, 0, 0));

        allLaws.add(new Law("Религиозная нетерпимость",
                "Запрет других религий, но порождает конфликты",
                "Догматика", null,
                0, -1, 0, -2, 0, 3));

        allLaws.add(new Law("Военный налог",
                "Дополнительный налог на войну, но истощает население",
                "Империя", "Империя",
                0, 0, 2, -2, -1, 0));

        allLaws.add(new Law("Цензура",
                "Контроль над информацией, но убивает свободу мысли",
                "Империя", "Империя",
                0, -2, 0, 0, 2, 0));

        allLaws.add(new Law("Рабство",
                "Использование рабов, но разрушает мораль",
                "Право", null,
                0, -1, 2, -2, -2, 0));

        allLaws.add(new Law("Инквизиция (закон)",
                "Борьба с ересью, но террор вызывает страх",
                "Инквизиция", "Монархия",
                0, 0, 0, -2, 3, 4));

        allLaws.add(new Law("Религиозный орден (закон)",
                "Поддержка монашества, но ослабляет армию",
                "Религиозный орден", null,
                0, 1, -1, 0, 1, 3));

        allLaws.add(new Law("Имперский культ (закон)",
                "Обожествление императора, но развращает власть",
                "Имперский культ", "Империя",
                0, 1, 0, 0, 2, 2));
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public String getCurrentGovernment() { return currentGovernment; }
    public List<Law> getActiveLaws() { return new ArrayList<>(activeLaws); }
    public List<Law> getAllLaws() { return new ArrayList<>(allLaws); }
    public int getRepealCooldown(Law law) { return repealedLaws.getOrDefault(law, 0); }

    public Law getPendingLaw() { return pendingLaw; }
    public int getPoliticalProcessTurns() { return politicalProcessTurns; }
    public int getPoliticalProcessMaxTurns() { return politicalProcessMaxTurns; }
    public boolean isPoliticalProcessActive() { return politicalProcessActive; }
    public String getPoliticalProcessStatus() { return politicalProcessStatus; }
    public Map<String, Integer> getLegitimacyModifiers() { return new HashMap<>(legitimacyModifiers); }
    public int getLegitimacyModifiersTotal() {
        return legitimacyModifiers.values().stream().mapToInt(Integer::intValue).sum();
    }

    // ========================================================================
    // Модификаторы легитимности (события)
    // ========================================================================

    public void addLegitimacyModifier(String event, int delta) {
        legitimacyModifiers.put(event, delta);
    }

    public void removeLegitimacyModifier(String event) {
        legitimacyModifiers.remove(event);
    }

    // ========================================================================
    // Доступные законы (с учётом политического процесса)
    // ========================================================================

    public List<Law> getAvailableLaws() {
        if (politicalProcessActive) {
            return new ArrayList<>();
        }
        List<Law> available = new ArrayList<>();
        for (Law law : allLaws) {
            if (activeLaws.contains(law)) continue;
            if (repealedLaws.containsKey(law)) continue;
            if (law.getRequiredTech() != null && !techRegistry.isResearched(law.getRequiredTech())) continue;
            if (law.getRequiredGovernment() != null && !law.getRequiredGovernment().equals(currentGovernment)) continue;
            available.add(law);
        }
        return available;
    }

    public List<Law> getTemporarilyUnavailableLaws() {
        List<Law> unavailable = new ArrayList<>();
        for (Law law : allLaws) {
            if (activeLaws.contains(law)) continue;
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

    // ========================================================================
    // Политический процесс
    // ========================================================================

    public boolean startPoliticalProcess(Law law) {
        if (politicalProcessActive) {
            controller.updateStatus("Политический процесс уже идёт. Дождитесь его завершения.");
            return false;
        }
        if (activeLaws.contains(law)) {
            controller.updateStatus("Этот закон уже принят.");
            return false;
        }
        if (repealedLaws.containsKey(law)) {
            controller.updateStatus("Этот закон временно недоступен.");
            return false;
        }
        if (law.getRequiredTech() != null && !techRegistry.isResearched(law.getRequiredTech())) {
            controller.updateStatus("Требуется технология: " + law.getRequiredTech());
            return false;
        }
        if (law.getRequiredGovernment() != null && !law.getRequiredGovernment().equals(currentGovernment)) {
            controller.updateStatus("Требуется форма правления: " + law.getRequiredGovernment());
            return false;
        }

        pendingLaw = law;
        politicalProcessTurns = 0;
        politicalProcessActive = true;
        politicalProcessStatus = "В процессе";
        politicalProcessMaxTurns = getProcessDuration();

        controller.updateStatus("Политический процесс начат: " + law.getName() +
                " (потребуется " + politicalProcessMaxTurns + " ходов)");
        return true;
    }

    private int getProcessDuration() {
        switch (currentGovernment) {
            case "Вождество": return 5;
            case "Республика": return 4;
            case "Олигархия": return 6;
            case "Монархия": return 3;
            case "Империя": return 2;
            default: return 5;
        }
    }

    public void updatePoliticalProcess() {
        if (!politicalProcessActive || pendingLaw == null) return;

        politicalProcessTurns++;

        if (politicalProcessTurns >= politicalProcessMaxTurns) {
            int legitimacy = controller.getGameState().getLegitimacy();
            double chance = calculateSuccessChance(legitimacy);
            double roll = Math.random();

            if (roll < chance) {
                if (adoptLawDirect(pendingLaw)) {
                    politicalProcessStatus = "Принят";
                    controller.updateStatus("Закон '" + pendingLaw.getName() + "' принят!");
                } else {
                    politicalProcessStatus = "Провален (ошибка)";
                    controller.updateStatus("Ошибка при принятии закона.");
                }
            } else {
                politicalProcessStatus = "Провален";
                repealedLaws.put(pendingLaw, 3);
                controller.updateStatus("Закон '" + pendingLaw.getName() + "' не прошёл. Он недоступен на 3 хода.");
            }

            pendingLaw = null;
            politicalProcessActive = false;
            politicalProcessTurns = 0;
        }
    }

    private double calculateSuccessChance(int legitimacy) {
        double base = 0.4 + (legitimacy / 100.0) * 0.4;
        switch (currentGovernment) {
            case "Империя": return Math.min(0.95, base + 0.15);
            case "Монархия": return Math.min(0.90, base + 0.10);
            case "Республика": return Math.min(0.85, base + 0.05);
            case "Олигархия": return Math.min(0.85, base + 0.05);
            default: return Math.min(0.80, base);
        }
    }

    private boolean adoptLawDirect(Law law) {
        if (activeLaws.contains(law)) return false;
        activeLaws.add(law);
        law.setActive(true);
        return true;
    }

    // ========================================================================
    // Отмена закона
    // ========================================================================

    public boolean repealLaw(Law law) {
        if (!activeLaws.contains(law)) return false;
        activeLaws.remove(law);
        law.setActive(false);
        repealedLaws.put(law, 10);
        return true;
    }

    // ========================================================================
    // Обновление таймеров (вызывается в конце хода)
    // ========================================================================

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

    // ========================================================================
    // Суммарные бонусы от законов
    // ========================================================================

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

    // ========================================================================
    // Сброс
    // ========================================================================

    public void reset() {
        activeLaws.clear();
        repealedLaws.clear();
        legitimacyModifiers.clear();
        currentGovernment = "Вождество";
        pendingLaw = null;
        politicalProcessActive = false;
        politicalProcessTurns = 0;
        politicalProcessStatus = "";
        for (Law law : allLaws) {
            law.setActive(false);
        }
    }
}
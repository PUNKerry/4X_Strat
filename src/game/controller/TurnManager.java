package game.controller;

import engine.infrastructure.GameState;
import game.model.city.City;
import game.model.research.TechNode;
import game.model.research.TechTree;
import game.model.world.World;
import javafx.application.Platform;

public class TurnManager {

    private final GameController controller;


    private int turnNumber = 1;
    private boolean isPlayerTurn = true;

    private TechNode currentTech = null;
    private TechNode currentSocial = null;
    private TechNode currentReligion = null;
    private int techInvested = 0;
    private int socialInvested = 0;
    private int religionInvested = 0;

    private int sciencePerTurn = 0;
    private int culturePerTurn = 0;
    private int faithPerTurn = 0;

    public TurnManager(GameController controller) {
        this.controller = controller;

    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public int getTurnNumber() { return turnNumber; }
    public boolean isPlayerTurn() { return isPlayerTurn; }
    public void setPlayerTurn(boolean playerTurn) { isPlayerTurn = playerTurn; }

    public TechNode getCurrentTech() { return currentTech; }
    public TechNode getCurrentSocial() { return currentSocial; }
    public TechNode getCurrentReligion() { return currentReligion; }
    public int getTechInvested() { return techInvested; }
    public int getSocialInvested() { return socialInvested; }
    public int getReligionInvested() { return religionInvested; }

    public int getSciencePerTurn() { return sciencePerTurn; }
    public int getCulturePerTurn() { return culturePerTurn; }
    public int getFaithPerTurn() { return faithPerTurn; }

    public int getTechCost() { return currentTech != null ? currentTech.getCost() : 0; }
    public int getSocialCost() { return currentSocial != null ? currentSocial.getCost() : 0; }
    public int getReligionCost() { return currentReligion != null ? currentReligion.getCost() : 0; }

    public int getTechTurnsLeft() {
        if (currentTech == null || sciencePerTurn == 0) return Integer.MAX_VALUE;
        int remaining = currentTech.getCost() - techInvested;
        return (int) Math.ceil((double) remaining / sciencePerTurn);
    }

    public int getSocialTurnsLeft() {
        if (currentSocial == null || culturePerTurn == 0) return Integer.MAX_VALUE;
        int remaining = currentSocial.getCost() - socialInvested;
        return (int) Math.ceil((double) remaining / culturePerTurn);
    }

    public int getReligionTurnsLeft() {
        if (currentReligion == null || faithPerTurn == 0) return Integer.MAX_VALUE;
        int remaining = currentReligion.getCost() - religionInvested;
        return (int) Math.ceil((double) remaining / faithPerTurn);
    }

    public double getTechProgress() {
        if (currentTech == null) return 0;
        return Math.min(1.0, (double) techInvested / currentTech.getCost());
    }

    public double getSocialProgress() {
        if (currentSocial == null) return 0;
        return Math.min(1.0, (double) socialInvested / currentSocial.getCost());
    }

    public double getReligionProgress() {
        if (currentReligion == null) return 0;
        return Math.min(1.0, (double) religionInvested / currentReligion.getCost());
    }

    // ========================================================================
    // Выбор исследований
    // ========================================================================

    public boolean selectTech(String techName) {
        TechTree techTree = controller.getTechTree();
        TechNode node = techTree.getNodeByName(techName);
        if (node == null || node.isResearched()) return false;
        for (String prereq : node.getPrerequisites()) {
            if (!techTree.isResearched(prereq)) return false;
        }
        currentTech = node;
        techInvested = 0;
        controller.updateUI();
        return true;
    }

    public boolean selectSocial(String socialName) {
        TechTree techTree = controller.getTechTree();
        TechNode node = techTree.getNodeByName(socialName);
        if (node == null || node.isResearched()) return false;
        for (String prereq : node.getPrerequisites()) {
            if (!techTree.isResearched(prereq)) return false;
        }
        if (!isSocialTechAvailable(node)) {
            controller.updateStatus("Не выполнены условия для изучения.");
            return false;
        }
        currentSocial = node;
        socialInvested = 0;
        controller.updateUI();
        return true;
    }

    public boolean selectReligion(String religionName) {
        TechTree techTree = controller.getTechTree();
        if (!techTree.isReligionUnlocked()) return false;
        TechNode node = techTree.getNodeByName(religionName);
        if (node == null || node.isResearched()) return false;
        for (String prereq : node.getPrerequisites()) {
            if (!techTree.isResearched(prereq)) return false;
        }
        currentReligion = node;
        religionInvested = 0;
        controller.updateUI();
        return true;
    }

    // ========================================================================
    // Полюса и условия
    // ========================================================================

    public double[] computePoleValues() {
        double science = sciencePerTurn;
        double faith = faithPerTurn;
        double culture = culturePerTurn;
        int cities = controller.getCities().size();
        int units = controller.getAllUnits().size();
        int techCount = 0;
        for (TechNode n : controller.getTechTree().getTechs()) {
            if (n.isResearched()) techCount++;
        }
        double scienceFaith = Math.min(100, (science + faith) * 2);
        double warPeace = Math.min(100, units * 10);
        double centralization = Math.min(100, cities * 20);
        double traditionReform = Math.min(100, culture * 5 + 20);
        double elitePeople = Math.min(100, cities * 10 + units * 2);
        double isolationOpen = Math.min(100, techCount * 5);
        double natureIndustry = Math.min(100, (science + culture) * 2);
        return new double[]{scienceFaith, warPeace, centralization, traditionReform, elitePeople, isolationOpen, natureIndustry};
    }

    // ========================================================================
    // Динамические условия для социальной ветки
    // ========================================================================

    public boolean isSocialTechAvailable(TechNode node) {
        String name = node.getName();
        GameController c = controller;
        switch (name) {
            case "Республика":
                return c.getCities().size() >= 3;
            case "Олигархия":
                return c.getCities().size() <= 5;
            case "Монархия":
                return c.getGameState().getLegitimacy() > 50 &&
                        !c.getAllUnits().isEmpty();
            case "Империя":
                boolean hasRepublic = c.getTechTree().isResearched("Республика");
                boolean hasOligarchy = c.getTechTree().isResearched("Олигархия");
                boolean hasMonarchy = c.getTechTree().isResearched("Монархия");
                return (hasRepublic || hasOligarchy || hasMonarchy) &&
                        c.getGameState().getLegitimacy() > 70 &&
                        c.getCities().size() >= 5 &&
                        c.getTechTree().isResearched("Империя");
            default:
                return true;
        }
    }

    // ========================================================================
    // Сброс состояния
    // ========================================================================

    public void reset() {
        turnNumber = 1;
        isPlayerTurn = true;
        currentTech = null;
        currentSocial = null;
        currentReligion = null;
        techInvested = 0;
        socialInvested = 0;
        religionInvested = 0;
        sciencePerTurn = 0;
        culturePerTurn = 0;
        faithPerTurn = 0;
    }

    // ========================================================================
    // Пересчёт доходов и легитимности
    // ========================================================================

    void recalcIncome() {
        int totalScience = 0, totalCulture = 0, totalFaith = 0;
        for (City city : controller.getCities()) {
            totalScience += city.getScienceOutput();
            totalCulture += city.getCultureOutput();
            totalFaith += city.getFaithOutput();
        }
        int totalFreeWorkers = 0;
        for (City city : controller.getCities()) {
            totalFreeWorkers += city.getFreeWorkers();
        }
        sciencePerTurn = totalScience + totalFreeWorkers / 2;
        culturePerTurn = totalCulture + totalFreeWorkers / 2;
        faithPerTurn = totalFaith;
    }

    void recalculateLegitimacy() {
        GameState gameState = controller.getGameState();
        TechTree techTree = controller.getTechTree();
        if (!techTree.isLegitimacyUnlocked()) {
            gameState.setLegitimacy(0);
            return;
        }

        int leg = 50;
        leg += controller.getCities().size() * 5;
        int totalPop = 0;
        for (City city : controller.getCities()) totalPop += city.getPopulation();
        leg += totalPop / 1000;
        int techCount = 0;
        for (TechNode node : techTree.getTechs()) {
            if (node.isResearched()) techCount++;
        }
        leg += techCount * 2;
        leg += culturePerTurn / 2;
        // Бонус от законов
        leg += controller.getGovernmentManager().getTotalLegitimacyBonus();
        leg = Math.min(100, Math.max(0, leg));
        gameState.setLegitimacy(leg);
    }

    // ========================================================================
    // Завершение исследований
    // ========================================================================

    private void completeTech() {
        if (currentTech == null) return;
        if (controller.getAdvisor() != null && currentTech.getAdvisorMessage() != null) {
            controller.getAdvisor().showTechComplete(currentTech);
        }
        currentTech.setResearched(true);
        currentTech = null;
        techInvested = 0;
        recalcIncome();
        controller.updateUI();
    }

    private void completeSocial() {
        if (currentSocial == null) return;
        if (controller.getAdvisor() != null && currentSocial.getAdvisorMessage() != null) {
            controller.getAdvisor().showTechComplete(currentSocial);
        }
        currentSocial.setResearched(true);
        currentSocial = null;
        socialInvested = 0;
        recalcIncome();
        recalculateLegitimacy();
        controller.updateUI();
    }

    private void completeReligion() {
        if (currentReligion == null) return;
        if (controller.getAdvisor() != null && currentReligion.getAdvisorMessage() != null) {
            controller.getAdvisor().showTechComplete(currentReligion);
        }
        currentReligion.setResearched(true);
        currentReligion = null;
        religionInvested = 0;
        recalcIncome();
        controller.updateUI();
    }

    // ========================================================================
    // ЗАВЕРШЕНИЕ ХОДА
    // ========================================================================

    public void endTurn() {
        if (!isPlayerTurn) return;
        isPlayerTurn = false;
        turnNumber++;

        recalcIncome();

        GameState gameState = controller.getGameState();
        TechTree techTree = controller.getTechTree();

        gameState.addScience(sciencePerTurn);
        gameState.addCulture(culturePerTurn);
        if (techTree.isMoneyUnlocked()) {
            gameState.addTreasury(0);
        }
        if (techTree.isReligionUnlocked()) {
            gameState.addPiety(faithPerTurn);
        }

        // Прогресс исследований
        if (currentTech != null) {
            techInvested += sciencePerTurn;
            if (techInvested >= currentTech.getCost()) {
                completeTech();
            }
        }
        if (currentSocial != null) {
            socialInvested += culturePerTurn;
            if (socialInvested >= currentSocial.getCost()) {
                completeSocial();
            }
        }
        if (currentReligion != null && techTree.isReligionUnlocked()) {
            religionInvested += faithPerTurn;
            if (religionInvested >= currentReligion.getCost()) {
                completeReligion();
            }
        }

        // Обновление городов
        World world = controller.getWorld();
        for (City city : controller.getCities()) {
            int totalFood = city.calculateFood(world);
            int totalProd = city.calculateProduction(world);
            city.updatePopulation(totalFood);
            city.advanceProduction(totalProd);
            if (city.getJustFinishedSettler()) {
                controller.spawnSettlerNearCity(city);
                city.setJustFinishedSettler(false);
            }
            if (city.getJustFinishedScout()) {
                controller.spawnScoutNearCity(city);
                city.setJustFinishedScout(false);
            }
            city.updateExpansion();
        }

        // Обновление юнитов
        controller.updateUnitsEndTurn();

        // Сброс накоплений
        gameState.resetScience();
        gameState.resetCulture();

        // Пересчёт легитимности и тумана
        recalculateLegitimacy();
        controller.recalculateFog();
        controller.getHistoryTracker().addEntry(sciencePerTurn, culturePerTurn, faithPerTurn);

        // Обновление таймеров отменённых законов
        controller.getGovernmentManager().updateRepealedLaws();

        controller.updateUI();

        // Пауза перед следующим ходом
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                isPlayerTurn = true;
                controller.executeWaypointsMovement();
                controller.updateStatus("Ваш ход!");
                controller.updateUI();
            });
        }).start();
    }
}
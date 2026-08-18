package game.model.city;

import engine.core.GameObject;
import game.controller.GameController;
import game.model.registry.*;
import game.model.research.TechTree;
import game.model.unit.Unit;
import game.model.world.Hex;
import game.model.world.Improvement;
import game.model.world.Tile;
import game.model.world.World;

import java.util.*;

public class City {
    private String name;
    private Hex center;
    private transient World world;
    private transient GameController controller;

    // Менеджеры
    private final CityProductionManager productionManager;
    private final CityPopulationManager populationManager;
    private final CityTerritoryManager territoryManager;
    private final CityWorkersManager workersManager;
    private final CityImprovementsManager improvementManager;

    // Жильё (базовое)
    private int housingCapacity = 1000;

    private static final String[] TYPE_NAMES = {"Поселение", "Малый городок", "Город", "Мегаполис"};

    // ========================================================================
    // Конструктор
    // ========================================================================

    public City(Hex center, String name, World world, GameController controller,
                ImprovementRegistry improvementRegistry,
                DistrictRegistry districtRegistry,
                CenterImprovementRegistry centerRegistry,
                TechRegistry techRegistry) {
        this.center = center;
        this.name = name;
        this.world = world;
        this.controller = controller;

        // Инициализация менеджеров
        this.productionManager = new CityProductionManager(this);
        int initialPopulation = 100 + (int)(Math.random() * 401);
        int initialHappiness = 50;
        this.populationManager = new CityPopulationManager(this, initialPopulation, initialHappiness);
        this.territoryManager = new CityTerritoryManager(this, world, controller, center);
        this.workersManager = new CityWorkersManager(this);
        this.improvementManager = new CityImprovementsManager(this, world, controller,
                improvementRegistry, districtRegistry, centerRegistry, techRegistry);

        // Назначаем одного горожанина в центр
        workersManager.initCenter(center);
    }

    // ========================================================================
    // Геттеры для основных данных
    // ========================================================================

    public String getName() { return name; }
    public Hex getCenter() { return center; }
    public GameController getController() { return controller; }

    // ========================================================================
    // Делегирование населению
    // ========================================================================

    public int getPopulation() { return populationManager.getPopulation(); }
    public void setPopulation(int population) { populationManager.setPopulation(population); }
    public int getHappiness() { return populationManager.getHappiness() + improvementManager.getHappinessBonus(); }
    public void setHappiness(int happiness) { populationManager.setHappiness(happiness); }
    public int getPopulationChange() { return populationManager.getPopulationChange(); }
    public boolean isStarving() { return populationManager.isStarving(); }

    // ========================================================================
    // Делегирование территории
    // ========================================================================

    public Set<Hex> getTiles() { return territoryManager.getTiles(); }
    public int getExpansionTimer() { return territoryManager.getExpansionTimer(); }
    public boolean hasFreshWater() { return territoryManager.hasFreshWater(); }

    // ========================================================================
    // Делегирование производства
    // ========================================================================

    public String getProductionItem() { return productionManager.getProductionItem(); }
    public int getProductionProgress() { return productionManager.getProductionProgress(); }
    public int getProductionTarget() { return productionManager.getProductionTarget(); }
    public boolean getJustFinishedSettler() { return productionManager.getJustFinishedSettler(); }
    public void setJustFinishedSettler(boolean value) { productionManager.setJustFinishedSettler(value); }
    public boolean getJustFinishedScout() { return productionManager.getJustFinishedScout(); }
    public void setJustFinishedScout(boolean value) { productionManager.setJustFinishedScout(value); }
    public int getProductionAllocation() { return productionManager.getProductionAllocation(); }
    public void setProductionAllocation(int allocation) { productionManager.setProductionAllocation(allocation); }

    public Queue<Improvement> getImprovementQueue() { return productionManager.getImprovementQueue(); }
    public Queue<String> getUnitQueue() { return productionManager.getUnitQueue(); }
    public Queue<String> getProjectQueue() { return productionManager.getProjectQueue(); }
    public Queue<District> getDistrictQueue() { return productionManager.getDistrictQueue(); }
    public Queue<String> getCenterImprovementQueue() { return productionManager.getCenterImprovementQueue(); }
    public CityProductionManager getProductionManager() { return productionManager; }

    // ========================================================================
    // Делегирование улучшений (включая районы и центральные)
    // ========================================================================

    public Set<District> getCompletedDistricts() { return improvementManager.getCompletedDistricts(); }
    public Set<String> getCompletedCenterImprovements() { return improvementManager.getCompletedCenterImprovements(); }
    public int getLegitimacyBonus() { return improvementManager.getLegitimacyBonus(); }

    // ========================================================================
    // Делегирование рабочих
    // ========================================================================

    public Map<Hex, Integer> getAssignedCitizens() { return workersManager.getAssignedCitizens(); }
    public int getReservedWorkers() { return workersManager.getReservedWorkers(); }
    public void setReservedWorkers(int reserved) { workersManager.setReservedWorkers(reserved); }
    public void addReservedWorkers(int amount) { workersManager.addReservedWorkers(amount); }
    public void subtractReservedWorkers(int amount) { workersManager.subtractReservedWorkers(amount); }
    public int getFreeWorkers() { return workersManager.getFreeWorkers(); }
    public boolean hasEnoughWorkers(int needed) { return workersManager.hasEnoughWorkers(needed); }
    public int getMaxCitizens() { return workersManager.getMaxCitizens(); }
    public int getUsedCitizens() { return workersManager.getUsedCitizens(); }
    public int getCitizensPerTile() { return workersManager.getCitizensPerTile(); }

    // Методы назначения горожан
    public boolean assignCitizen(Hex hex) { return workersManager.assignCitizen(hex); }
    public boolean unassignCitizen(Hex hex) { return workersManager.unassignCitizen(hex); }
    public int getAssignedCount(Hex hex) { return workersManager.getAssignedCount(hex); }
    public int getTotalAssigned() { return workersManager.getTotalAssigned(); }
    public int getRequiredWorkers(Hex hex) { return workersManager.getRequiredWorkers(hex); }
    public boolean isFullyAssigned(Hex hex) { return workersManager.isFullyAssigned(hex); }
    public Set<Hex> getAssignedTiles() { return workersManager.getAssignedTiles(); }
    public boolean isAssigned(Hex hex) { return workersManager.isAssigned(hex); }

    // ========================================================================
    // Тип города
    // ========================================================================

    public String getType() {
        int pop = populationManager.getPopulation();
        if (pop < 10000) return TYPE_NAMES[0];
        if (pop < 100000) return TYPE_NAMES[1];
        if (pop < 1000000) return TYPE_NAMES[2];
        return TYPE_NAMES[3];
    }

    // ========================================================================
    // Жильё
    // ========================================================================

    public int getHousingCapacity() {
        return housingCapacity + improvementManager.getHousingBonus();
    }

    public void addHousingCapacity(int amount) {
        this.housingCapacity += amount;
    }

    // ========================================================================
    // Расчёт ресурсов (еда, производство, наука, культура, вера)
    // ========================================================================

    public int calculateFood(World world) {
        int total = 0;
        Set<Hex> tiles = territoryManager.getTiles();
        Map<Hex, Integer> assigned = workersManager.getAssignedCitizens();
        Map<Hex, Improvement> completedImprovements = improvementManager.getCompletedImprovements();

        for (Map.Entry<Hex, Integer> entry : assigned.entrySet()) {
            Hex hex = entry.getKey();
            int count = entry.getValue();
            Tile tile = findTile(world, hex);
            if (tile != null) {
                total += tile.getTerrain().getFood() * count;
                Improvement imp = completedImprovements.get(hex);
                if (imp != null) {
                    total += imp.getFoodBonus() * count;
                }
            }
        }

        // Бонусы от центральных улучшений
        total += improvementManager.getFoodBonus();

        // Штраф за отсутствие пресной воды
        if (!territoryManager.hasFreshWater()) {
            total = (int)(total * 0.5);
        }
        return total;
    }

    public int calculateProduction(World world) {
        int total = 0;
        Map<Hex, Integer> assigned = workersManager.getAssignedCitizens();
        Map<Hex, Improvement> completedImprovements = improvementManager.getCompletedImprovements();

        for (Map.Entry<Hex, Integer> entry : assigned.entrySet()) {
            Hex hex = entry.getKey();
            int count = entry.getValue();
            Tile tile = findTile(world, hex);
            if (tile != null) {
                total += tile.getTerrain().getProduction() * count;
                Improvement imp = completedImprovements.get(hex);
                if (imp != null) {
                    total += imp.getProductionBonus() * count;
                }
            }
        }

        // Бонусы от центральных улучшений
        total += improvementManager.getProductionBonus();
        return total;
    }

    public int getScienceOutput() {
        int pop = populationManager.getPopulation();
        int base = (int)(pop / 1000.0);
        base += improvementManager.getScienceBonus();
        return base;
    }

    public int getFaithOutput() {
        int base = (int)(populationManager.getPopulation() / 2000.0);
        base += improvementManager.getFaithBonus();
        return base;
    }

    public int getCultureOutput() {
        int pop = populationManager.getPopulation();
        int base = (int)(pop * 0.7 / 1000.0);
        base += improvementManager.getCultureBonus();
        return base;
    }

    public int getGoldOutput() {
        // Пока нет бонусов от улучшений, но можно добавить
        return (int)(populationManager.getPopulation() * 0.3 / 1000.0);
    }

    // ========================================================================
    // Вспомогательный метод поиска тайла
    // ========================================================================

    private Tile findTile(World world, Hex hex) {
        for (GameObject obj : world.getAllObjects()) {
            if (obj instanceof Tile) {
                Tile tile = (Tile) obj;
                if (tile.getHex().equals(hex)) {
                    return tile;
                }
            }
        }
        return null;
    }

    // ========================================================================
    // Методы улучшений (делегирование)
    // ========================================================================

    public boolean canBuildImprovement(Hex hex, Improvement.Type type) {
        return improvementManager.canBuildImprovement(hex, type);
    }

    public List<Hex> getAvailableTilesForImprovement(Improvement.Type type) {
        return improvementManager.getAvailableTilesForImprovement(type);
    }

    public boolean startImprovement(Hex hex, Improvement.Type type, TechTree techTree) {
        // techTree используется только для проверки в старом коде, но мы используем регистры
        return improvementManager.startImprovement(hex, type);
    }

    void completeImprovement(Improvement imp) {
        improvementManager.completeImprovement(imp);
    }

    public Improvement getImprovementAt(Hex hex) {
        return improvementManager.getImprovementAt(hex);
    }

    public boolean isImprovementInProgress(Hex hex) {
        return improvementManager.isImprovementInProgress(hex);
    }

    public boolean hasCompletedImprovement(Hex hex) {
        return improvementManager.hasCompletedImprovement(hex);
    }

    // ========================================================================
    // Методы районов (делегирование)
    // ========================================================================

    public boolean canBuildDistrict(District.Type type) {
        return improvementManager.canBuildDistrict(type);
    }

    public List<Hex> getAvailableTilesForDistrict(District.Type type) {
        return improvementManager.getAvailableTilesForDistrict(type);
    }

    public boolean startDistrict(District.Type type, Hex hex, TechTree techTree) {
        return improvementManager.startDistrict(type, hex);
    }

    void completeDistrict(District district) {
        improvementManager.completeDistrict(district);
    }

    public void addDistrictToQueue(District district) {
        improvementManager.addDistrictToQueue(district);
    }

    // ========================================================================
    // Методы центральных улучшений (делегирование)
    // ========================================================================

    public boolean canBuildCenterImprovement(String name, TechTree techTree) {
        return improvementManager.canBuildCenterImprovement(name);
    }

    public void addCenterImprovementToQueue(String name) {
        improvementManager.addCenterImprovementToQueue(name);
    }

    void completeCenterImprovement(String name) {
        improvementManager.completeCenterImprovement(name);
    }

    // ========================================================================
    // Доступность для UI
    // ========================================================================

    public String getUnitAvailability(String unitType, TechTree techTree) {
        int pop = populationManager.getPopulation();
        if ("settler".equals(unitType)) {
            if (pop < 500) return "Недостаточно населения (нужно 500)";
            if (productionManager.getProductionItem() != null || !productionManager.getUnitQueue().isEmpty())
                return "Очередь производства занята";
            return null;
        }
        if ("scout".equals(unitType)) {
            if (pop < 50) return "Недостаточно населения (нужно 50)";
            return null;
        }
        int cost = CityProductionManager.getUnitPopulationCost(unitType);
        if (pop < cost) return "Недостаточно населения (нужно " + cost + ")";
        String required = Unit.getRequiredTech(unitType);
        if (required != null && !techTree.isResearched(required)) {
            return "Требуется технология: " + required;
        }
        return null;
    }

    public String getImprovementAvailability(Improvement.Type type, TechTree techTree) {
        // Используем регистры, но для обратной совместимости оставим проверку через менеджер
        if (!improvementManager.canBuildImprovement(null, type)) {
            // Не можем проверить конкретную клетку, поэтому проверим наличие доступных
            List<Hex> tiles = getAvailableTilesForImprovement(type);
            if (tiles.isEmpty()) {
                return "Нет подходящих клеток в территории города";
            }
            // Если доступны, значит проблема в технологии или рабочих
            ImprovementData data = improvementManager.getImprovementRegistry().get(type);
            if (data != null && !data.isTechAvailable(new TechRegistry(techTree))) {
                return "Требуется технология: " + data.getRequiredTech();
            }
            return "Недостаточно свободных рабочих";
        }
        return null;
    }

    public String getDistrictAvailability(District.Type type, TechTree techTree) {
        if (!improvementManager.canBuildDistrict(type)) {
            DistrictData data = improvementManager.getDistrictRegistry().get(type);
            if (data != null && !data.isTechAvailable(new TechRegistry(techTree))) {
                return "Требуется технология: " + data.getRequiredTech();
            }
            List<Hex> tiles = getAvailableTilesForDistrict(type);
            if (tiles.isEmpty()) {
                return "Нет свободных клеток для района";
            }
            return "Недостаточно свободных рабочих";
        }
        return null;
    }

    // ========================================================================
    // Обновление населения и расширения
    // ========================================================================

    public void updatePopulation(int totalFood) {
        populationManager.updatePopulation(totalFood);
    }

    public void updateExpansion() {
        int happiness = populationManager.getHappiness();
        int pop = populationManager.getPopulation();
        boolean isStarving = populationManager.isStarving();
        territoryManager.updateExpansion(happiness, pop, housingCapacity, isStarving);
    }

    public void checkFreshWater() {
        territoryManager.checkFreshWater();
    }

    // ========================================================================
    // Производственные методы
    // ========================================================================

    public void addUnit(String type) {
        productionManager.addUnit(type);
    }

    public void addImprovement(Improvement imp) {
        productionManager.addImprovement(imp);
    }

    public void addProject(String project) {
        productionManager.addProject(project);
    }

    public void advanceProduction(int totalProd) {
        productionManager.advanceProduction(totalProd);
    }

    public boolean canProduceSettler() {
        return productionManager.canProduceSettler();
    }

    public boolean canProduceUnit(String unitType, TechTree techTree) {
        return productionManager.canProduceUnit(unitType, techTree);
    }

    public static int getUnitPopulationCost(String unitType) {
        return CityProductionManager.getUnitPopulationCost(unitType);
    }

    public void startProduction(String item, int cost) {
        productionManager.startProduction(item, cost);
    }

    // ========================================================================
    // Сброс (для новой игры)
    // ========================================================================

    public void reset() {
        populationManager.reset(100 + (int)(Math.random() * 401), 50);
        territoryManager.reset(center);
        workersManager.reset();
        improvementManager.reset();
        productionManager.setJustFinishedSettler(false);
        productionManager.setJustFinishedScout(false);
        housingCapacity = 1000;
    }
}
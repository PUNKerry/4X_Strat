package game.model.city;

import game.model.unit.Unit;
import game.model.research.TechTree;
import game.model.world.Improvement;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Управление производством в городе: очереди юнитов, улучшений, районов, проектов,
 * прогресс текущего производства, флаги завершения.
 */
public class CityProductionManager {

    private final City city;

    // Очереди
    private Queue<Improvement> improvementQueue = new LinkedList<>();
    private Queue<String> unitQueue = new LinkedList<>();
    private Queue<String> projectQueue = new LinkedList<>();
    private Queue<District> districtQueue = new LinkedList<>();
    private Queue<String> centerImprovementQueue = new LinkedList<>();

    private String currentProduction = null;
    private int productionProgress = 0;
    private int productionTarget = 0;
    private boolean justFinishedSettler = false;
    private boolean justFinishedScout = false;

    private int productionAllocation = 50;

    public CityProductionManager(City city) {
        this.city = city;
    }

    // ========================================================================
    // Геттеры для очередей и состояния
    // ========================================================================

    public Queue<Improvement> getImprovementQueue() { return improvementQueue; }
    public Queue<String> getUnitQueue() { return unitQueue; }
    public Queue<String> getProjectQueue() { return projectQueue; }
    public Queue<District> getDistrictQueue() { return districtQueue; }
    public Queue<String> getCenterImprovementQueue() { return centerImprovementQueue; }

    public String getProductionItem() { return currentProduction; }
    public int getProductionProgress() { return productionProgress; }
    public int getProductionTarget() { return productionTarget; }
    public boolean getJustFinishedSettler() { return justFinishedSettler; }
    public void setJustFinishedSettler(boolean value) { this.justFinishedSettler = value; }
    public boolean getJustFinishedScout() { return justFinishedScout; }
    public void setJustFinishedScout(boolean value) { this.justFinishedScout = value; }
    public int getProductionAllocation() { return productionAllocation; }
    public void setProductionAllocation(int allocation) { this.productionAllocation = Math.max(0, Math.min(100, allocation)); }

    // ========================================================================
    // Добавление в очереди
    // ========================================================================

    public void addUnit(String type) {
        unitQueue.add(type);
        if (currentProduction == null) startNextProduction();
    }

    public void addImprovement(Improvement imp) {
        improvementQueue.add(imp);
        if (currentProduction == null) startNextProduction();
    }

    public void addProject(String project) {
        projectQueue.add(project);
        if (currentProduction == null) startNextProduction();
    }

    public void addDistrictToQueue(District district) {
        districtQueue.add(district);
        if (currentProduction == null) startNextProduction();
    }

    public void addCenterImprovementToQueue(String name) {
        centerImprovementQueue.add(name);
        if (currentProduction == null) startNextProduction();
    }

    // ========================================================================
    // Запуск следующего производства
    // ========================================================================

    private void startNextProduction() {
        if (!unitQueue.isEmpty()) {
            currentProduction = "unit_" + unitQueue.peek();
            productionTarget = 50;
            productionProgress = 0;
        } else if (!improvementQueue.isEmpty()) {
            Improvement imp = improvementQueue.peek();
            currentProduction = "improvement_" + imp.getType().name();
            productionTarget = imp.getCost();
            productionProgress = 0;
        } else if (!districtQueue.isEmpty()) {
            District dist = districtQueue.peek();
            currentProduction = "district_" + dist.getType().name();
            productionTarget = dist.getCost();
            productionProgress = 0;
        } else if (!centerImprovementQueue.isEmpty()) {
            currentProduction = "center_" + centerImprovementQueue.peek();
            productionTarget = 40;
            productionProgress = 0;
        } else if (!projectQueue.isEmpty()) {
            currentProduction = "project_" + projectQueue.peek();
            productionTarget = 40;
            productionProgress = 0;
        } else {
            currentProduction = null;
            productionProgress = 0;
            productionTarget = 0;
        }
    }

    // ========================================================================
    // Продвижение производства
    // ========================================================================

    public void advanceProduction(int totalProd) {
        if (currentProduction == null) {
            if (!unitQueue.isEmpty() || !improvementQueue.isEmpty() ||
                    !districtQueue.isEmpty() || !centerImprovementQueue.isEmpty() ||
                    !projectQueue.isEmpty()) {
                startNextProduction();
            }
            return;
        }

        productionProgress += totalProd;
        if (productionProgress >= productionTarget) {
            if (currentProduction.startsWith("unit_")) {
                String type = currentProduction.substring(5);
                if ("settler".equals(type)) {
                    justFinishedSettler = true;
                } else if ("scout".equals(type)) {
                    city.setPopulation(Math.max(0, city.getPopulation() - 50));
                    justFinishedScout = true;
                } else {
                    int cost = getUnitPopulationCost(type);
                    city.setPopulation(Math.max(0, city.getPopulation() - cost));
                    if (city.getController() != null) {
                        city.getController().spawnUnitNearCity(city, type);
                        if (city.getController().getAdvisor() != null &&
                                city.getController().getAllUnits().size() == 1) {
                            city.getController().getAdvisor().showFatigueTutorial();
                        }
                    }
                }
                unitQueue.poll();
            } else if (currentProduction.startsWith("improvement_")) {
                Improvement imp = improvementQueue.poll();
                if (imp != null) {
                    city.completeImprovement(imp);
                }
            } else if (currentProduction.startsWith("district_")) {
                District dist = districtQueue.poll();
                if (dist != null) {
                    city.completeDistrict(dist);
                }
            } else if (currentProduction.startsWith("center_")) {
                String name = currentProduction.substring(7);
                city.completeCenterImprovement(name);
                centerImprovementQueue.poll();
            } else if (currentProduction.startsWith("project_")) {
                projectQueue.poll();
            }
            currentProduction = null;
            productionProgress = 0;
            productionTarget = 0;
            startNextProduction();
        }
    }

    // ========================================================================
    // Проверка возможности производства юнитов
    // ========================================================================

    public boolean canProduceSettler() {
        return city.getPopulation() >= 500 && currentProduction == null && unitQueue.isEmpty();
    }

    public boolean canProduceUnit(String unitType, TechTree techTree) {
        if ("settler".equals(unitType)) return canProduceSettler();
        if ("scout".equals(unitType)) return city.getPopulation() >= 50;
        int cost = getUnitPopulationCost(unitType);
        if (city.getPopulation() < cost) return false;
        String required = Unit.getRequiredTech(unitType);
        if (required != null && !techTree.isResearched(required)) {
            return false;
        }
        return true;
    }

    // ========================================================================
    // Статический метод для стоимости населения
    // ========================================================================

    public static int getUnitPopulationCost(String unitType) {
        switch (unitType) {
            case "settler": return 0;
            case "scout": return 0;
            case "warrior": return 50;
            case "archer": return 50;
            case "chariot": return 60;
            case "bronze_swordsman": return 60;
            case "horseman": return 70;
            case "galley": return 80;
            case "battering_ram": return 80;
            default: return 0;
        }
    }

    // ========================================================================
    // Запуск производства (устаревший метод, оставлен для совместимости)
    // ========================================================================

    public void startProduction(String item, int cost) {
        if ("settler".equals(item)) addUnit("settler");
    }
}
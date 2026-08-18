package game.model.city;

import engine.core.GameObject;
import game.controller.GameController;
import game.model.registry.*;
import game.model.world.*;

import java.util.*;

public class CityImprovementsManager {

    private final City city;
    private final World world;
    private final GameController controller;

    private final ImprovementRegistry improvementRegistry;
    private final DistrictRegistry districtRegistry;
    private final CenterImprovementRegistry centerRegistry;
    private final TechRegistry techRegistry;

    public ImprovementRegistry getImprovementRegistry() { return improvementRegistry; }
    public TechRegistry getTechRegistry() { return techRegistry; }
    public DistrictRegistry getDistrictRegistry() { return districtRegistry; }

    private Map<Hex, Improvement> improvementsInProgress = new HashMap<>();
    private Map<Hex, Improvement> completedImprovements = new HashMap<>();
    private Set<District> completedDistricts = new HashSet<>();
    private Set<String> completedCenterImprovements = new HashSet<>();

    public CityImprovementsManager(City city, World world, GameController controller,
                                   ImprovementRegistry improvementRegistry,
                                   DistrictRegistry districtRegistry,
                                   CenterImprovementRegistry centerRegistry,
                                   TechRegistry techRegistry) {
        this.city = city;
        this.world = world;
        this.controller = controller;
        this.improvementRegistry = improvementRegistry;
        this.districtRegistry = districtRegistry;
        this.centerRegistry = centerRegistry;
        this.techRegistry = techRegistry;
    }

    public Set<District> getCompletedDistricts() { return completedDistricts; }
    public Set<String> getCompletedCenterImprovements() { return completedCenterImprovements; }
    public Map<Hex, Improvement> getCompletedImprovements() { return completedImprovements; }

    public boolean canBuildImprovement(Hex hex, Improvement.Type type) {
        ImprovementData data = improvementRegistry.get(type);
        if (data == null) return false;
        if (!data.isTechAvailable(techRegistry)) return false;

        Set<Hex> tiles = city.getTiles();
        if (!tiles.contains(hex)) return false;
        if (hex.equals(city.getCenter())) return false;
        if (improvementsInProgress.containsKey(hex) || completedImprovements.containsKey(hex)) return false;

        Tile tile = findTile(world, hex);
        if (tile == null) return false;
        return data.isAllowedOnTerrain(tile.getTerrain());
    }

    public List<Hex> getAvailableTilesForImprovement(Improvement.Type type) {
        List<Hex> result = new ArrayList<>();
        Set<Hex> tiles = city.getTiles();
        for (Hex hex : tiles) {
            if (canBuildImprovement(hex, type)) result.add(hex);
        }
        return result;
    }

    public boolean startImprovement(Hex hex, Improvement.Type type) {
        ImprovementData data = improvementRegistry.get(type);
        if (data == null) return false;
        if (!canBuildImprovement(hex, type)) return false;

        int workersNeeded = data.getWorkersForConstruction();
        if (!city.hasEnoughWorkers(workersNeeded)) return false;

        Improvement imp = new Improvement(type, hex);
        improvementsInProgress.put(hex, imp);
        city.addImprovement(imp);
        city.addReservedWorkers(workersNeeded);
        return true;
    }

    void completeImprovement(Improvement imp) {
        Hex hex = imp.getTargetHex();
        improvementsInProgress.remove(hex);
        imp.setUnderConstruction(false);
        completedImprovements.put(hex, imp);
        ImprovementData data = improvementRegistry.get(imp.getType());
        if (data != null) {
            city.subtractReservedWorkers(data.getWorkersForConstruction());
        }
        Tile tile = findTile(world, hex);
        if (tile != null) {
            tile.setImprovement(imp);
        }
        // Реакция групп интересов на постройку улучшения
        controller.getGovernmentManager().onImprovementBuilt(imp.getType().name());
    }

    public Improvement getImprovementAt(Hex hex) {
        if (completedImprovements.containsKey(hex)) return completedImprovements.get(hex);
        if (improvementsInProgress.containsKey(hex)) return improvementsInProgress.get(hex);
        return null;
    }

    public boolean isImprovementInProgress(Hex hex) {
        return improvementsInProgress.containsKey(hex);
    }

    public boolean hasCompletedImprovement(Hex hex) {
        return completedImprovements.containsKey(hex);
    }

    // ========================================================================
    // Районы
    // ========================================================================

    public boolean canBuildDistrict(District.Type type) {
        DistrictData data = districtRegistry.get(type);
        if (data == null) return false;
        return data.isTechAvailable(techRegistry);
    }

    public List<Hex> getAvailableTilesForDistrict(District.Type type) {
        List<Hex> result = new ArrayList<>();
        Set<Hex> tiles = city.getTiles();
        for (Hex hex : tiles) {
            if (hex.equals(city.getCenter())) continue;
            if (improvementsInProgress.containsKey(hex) || completedImprovements.containsKey(hex)) continue;
            Tile tile = findTile(world, hex);
            if (tile == null || tile.getTerrain() == TerrainType.OCEAN) continue;
            boolean occupied = false;
            for (District d : completedDistricts) {
                if (d.getLocation() != null && d.getLocation().equals(hex)) {
                    occupied = true;
                    break;
                }
            }
            if (occupied) continue;
            result.add(hex);
        }
        return result;
    }

    public boolean startDistrict(District.Type type, Hex hex) {
        if (!city.canBuildDistrict(type)) return false;
        DistrictData data = districtRegistry.get(type);
        if (data == null) return false;
        if (!canBuildDistrict(type)) return false;
        if (hex.equals(city.getCenter())) return false;
        if (improvementsInProgress.containsKey(hex) || completedImprovements.containsKey(hex)) return false;
        Tile tile = findTile(world, hex);
        if (tile == null || tile.getTerrain() == TerrainType.OCEAN) return false;
        for (District d : completedDistricts) {
            if (d.getLocation() != null && d.getLocation().equals(hex)) return false;
        }
        int workersNeeded = data.getWorkersRequired();
        if (!city.hasEnoughWorkers(workersNeeded)) return false;

        District district = new District(type, hex);
        city.addDistrictToQueue(district);
        city.addReservedWorkers(workersNeeded);
        return true;
    }

    void completeDistrict(District district) {
        district.setUnderConstruction(false);
        completedDistricts.add(district);
        DistrictData data = districtRegistry.get(district.getType());
        if (data != null) {
            city.subtractReservedWorkers(data.getWorkersRequired());
            if (data.getHousingBonus() > 0) {
                city.addHousingCapacity(data.getHousingBonus());
            }
        }
        Tile tile = findTile(world, district.getLocation());
        if (tile != null) {
            tile.setDistrict(district);
        }
        // Районы тоже могут считаться улучшениями – вызовем onImprovementBuilt с типом "DISTRICT"
        controller.getGovernmentManager().onImprovementBuilt("DISTRICT_" + district.getType().name());
    }

    public void addDistrictToQueue(District district) {
        city.getProductionManager().addDistrictToQueue(district);
    }

    // ========================================================================
    // Центральные улучшения
    // ========================================================================

    public boolean canBuildCenterImprovement(String name) {
        CenterImprovementData data = centerRegistry.get(name);
        if (data == null) return false;
        if (completedCenterImprovements.contains(name)) return false;
        return data.isTechAvailable(techRegistry);
    }

    public void addCenterImprovementToQueue(String name) {
        if (canBuildCenterImprovement(name)) {
            city.getProductionManager().addCenterImprovementToQueue(name);
        }
    }

    void completeCenterImprovement(String name) {
        if (!completedCenterImprovements.contains(name)) {
            completedCenterImprovements.add(name);
        }
        // Реакция групп на центральное улучшение
        controller.getGovernmentManager().onImprovementBuilt("CENTER_" + name);
    }

    // ========================================================================
    // Подсчёт бонусов
    // ========================================================================

    public int getFoodBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getFoodBonus();
            }
        }
        return bonus;
    }

    public int getProductionBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getProductionBonus();
            }
        }
        return bonus;
    }

    public int getScienceBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getScienceBonus();
            }
        }
        for (District d : completedDistricts) {
            DistrictData data = districtRegistry.get(d.getType());
            if (data != null) bonus += data.getScienceBonus();
        }
        return bonus;
    }

    public int getCultureBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getCultureBonus();
            }
        }
        for (District d : completedDistricts) {
            DistrictData data = districtRegistry.get(d.getType());
            if (data != null) bonus += data.getCultureBonus();
        }
        return bonus;
    }

    public int getFaithBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getFaithBonus();
            }
        }
        for (District d : completedDistricts) {
            DistrictData data = districtRegistry.get(d.getType());
            if (data != null) bonus += data.getFaithBonus();
        }
        return bonus;
    }

    public int getHappinessBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getHappinessBonus();
            }
        }
        return bonus;
    }

    public int getLegitimacyBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getLegitimacyBonus();
            }
        }
        return bonus;
    }

    public int getHousingBonus() {
        int bonus = 0;
        for (CenterImprovementData data : centerRegistry.getAll()) {
            if (completedCenterImprovements.contains(data.getName())) {
                bonus += data.getHousingBonus();
            }
        }
        for (District d : completedDistricts) {
            DistrictData data = districtRegistry.get(d.getType());
            if (data != null) bonus += data.getHousingBonus();
        }
        return bonus;
    }

    // ========================================================================
    // Вспомогательные методы
    // ========================================================================

    private Tile findTile(World world, Hex hex) {
        for (GameObject obj : world.getAllObjects()) {
            if (obj instanceof Tile) {
                Tile tile = (Tile) obj;
                if (tile.getHex().equals(hex)) return tile;
            }
        }
        return null;
    }

    public void reset() {
        improvementsInProgress.clear();
        completedImprovements.clear();
        completedDistricts.clear();
        completedCenterImprovements.clear();
    }
}
package game.controller;

import game.model.unit.Unit;
import game.model.city.City;
import game.model.registry.*;
import game.model.world.Hex;
import game.model.world.Tile;
import game.model.world.World;

import java.util.ArrayList;
import java.util.List;

public class CityManager {

    private final GameController controller;
    private World world;
    private final List<City> cities = new ArrayList<>();

    // Регистры
    private final ImprovementRegistry improvementRegistry;
    private final DistrictRegistry districtRegistry;
    private final CenterImprovementRegistry centerImprovementRegistry;
    private final TechRegistry techRegistry;

    public CityManager(GameController controller) {
        this.controller = controller;
        this.world = controller.getWorld();
        this.improvementRegistry = controller.getImprovementRegistry();
        this.districtRegistry = controller.getDistrictRegistry();
        this.centerImprovementRegistry = controller.getCenterImprovementRegistry();
        this.techRegistry = controller.getTechRegistry();
    }

    public List<City> getCities() {
        return cities;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void foundCity(Unit unit, String cityName) {
        if (unit == null || !unit.canFoundCity() || unit.getActionPoints() <= 0) return;

        Hex centerHex = unit.getCurrentHex();
        City existing = findCityAtHex(centerHex);
        if (existing != null) return;

        City city = new City(centerHex, cityName, world, controller,
                improvementRegistry, districtRegistry, centerImprovementRegistry, techRegistry);
        city.setPopulation(unit.getPopulation());
        cities.add(city);

        for (Hex hex : city.getTiles()) {
            Tile tile = controller.findTileAtHex(hex);
            if (tile != null) {
                tile.setCityTiles(city.getTiles());
                if (hex.equals(centerHex)) {
                    tile.setCityCenter(true);
                    tile.setCityName(cityName);
                }
            }
        }

        world.removeObject(unit);
        controller.getAllUnits().remove(unit);
        if (unit == controller.getPlayerUnit()) {
            controller.setPlayerUnit(null);
        }

        controller.clearHighlights();
        controller.selectUnit(null);

        if (controller.onUnitSelected != null) controller.onUnitSelected.run();
        if (controller.onCitySelected != null) controller.onCitySelected.run();
        controller.updateUI();
        controller.recalculateFog();

        if (controller.getAdvisor() != null && cities.size() == 1) {
            controller.getAdvisor().showFirstCityTutorial();
        }
        if (controller.getAdvisor() != null && !city.hasFreshWater()) {
            controller.getAdvisor().showFreshWaterWarning(cityName);
        }

        controller.recalcIncome();
        controller.recalculateLegitimacy();
        controller.updateUI();
    }

    public City findCityAtHex(Hex hex) {
        for (City city : cities) {
            if (city.getTiles().contains(hex)) return city;
        }
        return null;
    }

    public void addProjectToCity(City city, String project) {
        if (city == null) return;
        city.addProject(project);
        controller.updateUI();
    }

    public void reset() {
        cities.clear();
    }
}
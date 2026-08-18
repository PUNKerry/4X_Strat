package game.model.city;

public class CityPopulationManager {

    private final City city;

    private int population;
    private int happiness;
    private int populationChange;
    private boolean isStarving;

    public CityPopulationManager(City city, int initialPopulation, int initialHappiness) {
        this.city = city;
        this.population = Math.max(0, initialPopulation);
        this.happiness = Math.max(0, Math.min(100, initialHappiness));
        this.populationChange = 0;
        this.isStarving = false;
    }

    // ========================================================================
    // Геттеры и сеттеры
    // ========================================================================

    public int getPopulation() { return population; }
    public int getHappiness() { return happiness; }
    public int getPopulationChange() { return populationChange; }
    public boolean isStarving() { return isStarving; }

    public void setPopulation(int population) {
        this.population = Math.max(0, population);
    }

    public void setHappiness(int happiness) {
        this.happiness = Math.max(0, Math.min(100, happiness));
    }

    // ========================================================================
    // Обновление населения
    // ========================================================================

    public void updatePopulation(int totalFood) {
        double consumption = population / 1000.0;
        double surplus = totalFood - consumption;
        double growth = surplus * 0.5;
        double maxChange = population * 0.15;
        if (growth > maxChange) growth = maxChange;
        if (growth < -maxChange) growth = -maxChange;
        int newPop = (int)(population + growth);
        if (newPop < 0) newPop = 0;
        populationChange = (int)growth;
        population = newPop;
        isStarving = (surplus < 0);
        updateHappiness();
    }

    // ========================================================================
    // Обновление счастья
    // ========================================================================

    private void updateHappiness() {
        int housingCapacity = city.getHousingCapacity();
        int base = 50;
        if (isStarving) {
            happiness -= 2;
        }
        int excess = population - housingCapacity;
        if (excess > 0) {
            int penalty = (excess + 99) / 100;
            happiness -= penalty;
        }
        if (!isStarving && population <= housingCapacity && happiness < 75) {
            happiness += 1;
        }
        if (happiness > 100) happiness = 100;
        if (happiness < 0) happiness = 0;
    }

    // ========================================================================
    // Сброс
    // ========================================================================

    public void reset(int initialPopulation, int initialHappiness) {
        this.population = Math.max(0, initialPopulation);
        this.happiness = Math.max(0, Math.min(100, initialHappiness));
        this.populationChange = 0;
        this.isStarving = false;
    }
}
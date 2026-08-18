package game.model.registry;

import game.model.city.District;

public class DistrictData {
    private final District.Type type;
    private final String displayName;
    private final int cost;
    private final String requiredTech;
    private final int scienceBonus;
    private final int cultureBonus;
    private final int housingBonus;
    private final int faithBonus;
    private final int workersRequired;

    public DistrictData(District.Type type, String displayName, int cost, String requiredTech,
                        int scienceBonus, int cultureBonus, int housingBonus, int faithBonus,
                        int workersRequired) {
        this.type = type;
        this.displayName = displayName;
        this.cost = cost;
        this.requiredTech = requiredTech;
        this.scienceBonus = scienceBonus;
        this.cultureBonus = cultureBonus;
        this.housingBonus = housingBonus;
        this.faithBonus = faithBonus;
        this.workersRequired = workersRequired;
    }

    public District.Type getType() { return type; }
    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public String getRequiredTech() { return requiredTech; }
    public int getScienceBonus() { return scienceBonus; }
    public int getCultureBonus() { return cultureBonus; }
    public int getHousingBonus() { return housingBonus; }
    public int getFaithBonus() { return faithBonus; }
    public int getWorkersRequired() { return workersRequired; }

    public boolean isTechAvailable(TechRegistry techRegistry) {
        return requiredTech == null || requiredTech.isEmpty() || techRegistry.isResearched(requiredTech);
    }
}
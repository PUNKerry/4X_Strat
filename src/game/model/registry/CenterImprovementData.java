package game.model.registry;

public class CenterImprovementData {
    private final String name;
    private final String requiredTech;
    private final int foodBonus;
    private final int productionBonus;
    private final int scienceBonus;
    private final int cultureBonus;
    private final int faithBonus;
    private final int housingBonus;
    private final int happinessBonus;
    private final int legitimacyBonus;
    private final String tooltip;

    public CenterImprovementData(String name, String requiredTech,
                                 int foodBonus, int productionBonus, int scienceBonus,
                                 int cultureBonus, int faithBonus, int housingBonus,
                                 int happinessBonus, int legitimacyBonus,
                                 String tooltip) {
        this.name = name;
        this.requiredTech = requiredTech;
        this.foodBonus = foodBonus;
        this.productionBonus = productionBonus;
        this.scienceBonus = scienceBonus;
        this.cultureBonus = cultureBonus;
        this.faithBonus = faithBonus;
        this.housingBonus = housingBonus;
        this.happinessBonus = happinessBonus;
        this.legitimacyBonus = legitimacyBonus;
        this.tooltip = tooltip;
    }

    public String getName() { return name; }
    public String getRequiredTech() { return requiredTech; }
    public int getFoodBonus() { return foodBonus; }
    public int getProductionBonus() { return productionBonus; }
    public int getScienceBonus() { return scienceBonus; }
    public int getCultureBonus() { return cultureBonus; }
    public int getFaithBonus() { return faithBonus; }
    public int getHousingBonus() { return housingBonus; }
    public int getHappinessBonus() { return happinessBonus; }
    public int getLegitimacyBonus() { return legitimacyBonus; }
    public String getTooltip() { return tooltip; }

    public boolean isTechAvailable(TechRegistry techRegistry) {
        return requiredTech == null || requiredTech.isEmpty() || techRegistry.isResearched(requiredTech);
    }
}
package game.model.government;

public class Law {
    private final String name;
    private final String description;
    private final String requiredTech;
    private final String requiredGovernment;
    private final int scienceBonus;
    private final int cultureBonus;
    private final int productionBonus;
    private final int happinessBonus;
    private final int legitimacyBonus;
    private final int faithBonus;
    private boolean isActive;

    public Law(String name, String description, String requiredTech, String requiredGovernment,
               int scienceBonus, int cultureBonus, int productionBonus,
               int happinessBonus, int legitimacyBonus, int faithBonus) {
        this.name = name;
        this.description = description;
        this.requiredTech = requiredTech;
        this.requiredGovernment = requiredGovernment;
        this.scienceBonus = scienceBonus;
        this.cultureBonus = cultureBonus;
        this.productionBonus = productionBonus;
        this.happinessBonus = happinessBonus;
        this.legitimacyBonus = legitimacyBonus;
        this.faithBonus = faithBonus;
        this.isActive = false;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRequiredTech() { return requiredTech; }
    public String getRequiredGovernment() { return requiredGovernment; }
    public int getScienceBonus() { return scienceBonus; }
    public int getCultureBonus() { return cultureBonus; }
    public int getProductionBonus() { return productionBonus; }
    public int getHappinessBonus() { return happinessBonus; }
    public int getLegitimacyBonus() { return legitimacyBonus; }
    public int getFaithBonus() { return faithBonus; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
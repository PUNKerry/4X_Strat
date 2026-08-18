package game.model.city;

import game.model.world.Hex;

public class District {
    public enum Type {
        HOUSING,      // жилища
        DISTRICT_1,   // район науки
        DISTRICT_2    // район культуры
    }

    private final Type type;
    private final String name;
    private final int cost;
    private final int scienceBonus;
    private final int cultureBonus;
    private final int housingBonus;
    private final Hex location;
    private boolean underConstruction;
    private final int workersRequired;

    public District(Type type, Hex location) {
        this.type = type;
        this.location = location;
        this.underConstruction = true;
        switch (type) {
            case HOUSING:
                this.name = "Жилища";
                this.cost = 50;
                this.scienceBonus = 0;
                this.cultureBonus = 0;
                this.housingBonus = 500;
                this.workersRequired = 4;
                break;
            case DISTRICT_1:
                this.name = "Район науки";
                this.cost = 60;
                this.scienceBonus = 1;
                this.cultureBonus = 0;
                this.housingBonus = 0;
                this.workersRequired = 4;
                break;
            case DISTRICT_2:
                this.name = "Район культуры";
                this.cost = 60;
                this.scienceBonus = 0;
                this.cultureBonus = 1;
                this.housingBonus = 0;
                this.workersRequired = 4;
                break;
            default:
                throw new IllegalArgumentException("Unknown district type");
        }
    }

    public Type getType() { return type; }
    public String getName() { return name; }
    public int getCost() { return cost; }
    public int getScienceBonus() { return scienceBonus; }
    public int getCultureBonus() { return cultureBonus; }
    public int getHousingBonus() { return housingBonus; }
    public Hex getLocation() { return location; }
    public boolean isUnderConstruction() { return underConstruction; }
    public void setUnderConstruction(boolean underConstruction) { this.underConstruction = underConstruction; }
    public int getWorkersRequired() { return workersRequired; }
}
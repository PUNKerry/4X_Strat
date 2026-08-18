package game.model.world;

public class Improvement {
    public enum Type {
        FARM, MINE, HOUSING,
        PASTURE, LUMBERMILL, QUARRY   // новые
    }

    private final Type type;
    private final int cost;
    private final int foodBonus;
    private final int productionBonus;
    private final Hex targetHex;
    private boolean underConstruction;
    private final int workersForConstruction;
    private final int workersToOperate;

    public Improvement(Type type, Hex targetHex) {
        this.type = type;
        this.targetHex = targetHex;
        this.underConstruction = true;
        switch (type) {
            case FARM:
                this.cost = 30;
                this.foodBonus = 2;
                this.productionBonus = 0;
                this.workersForConstruction = 4;
                this.workersToOperate = 4;
                break;
            case MINE:
                this.cost = 40;
                this.foodBonus = 0;
                this.productionBonus = 2;
                this.workersForConstruction = 5;
                this.workersToOperate = 1;
                break;
            case HOUSING:
                this.cost = 50;
                this.foodBonus = 0;
                this.productionBonus = 0;
                this.workersForConstruction = 4;
                this.workersToOperate = 2;
                break;
            default:
                throw new IllegalArgumentException("Unknown improvement type");
        }
    }

    public Type getType() { return type; }
    public int getCost() { return cost; }
    public int getFoodBonus() { return foodBonus; }
    public int getProductionBonus() { return productionBonus; }
    public Hex getTargetHex() { return targetHex; }
    public boolean isUnderConstruction() { return underConstruction; }
    public void setUnderConstruction(boolean underConstruction) { this.underConstruction = underConstruction; }
    public int getWorkersForConstruction() { return workersForConstruction; }
    public int getWorkersToOperate() { return workersToOperate; }
}
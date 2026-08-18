package engine.infrastructure;

public class GameState {
    private int science = 0;
    private int culture = 0;
    private int treasury = 0;
    private int piety = 0;
    private int legitimacy = 50; // от 0 до 100

    public int getScience() { return science; }
    public int getCulture() { return culture; }
    public int getTreasury() { return treasury; }
    public int getPiety() { return piety; }
    public int getLegitimacy() { return legitimacy; }

    public void addScience(int amount) { science += amount; }
    public void addCulture(int amount) { culture += amount; }
    public void addTreasury(int amount) { treasury += amount; }
    public void addPiety(int amount) { piety += amount; }

    public void resetScience() { science = 0; }
    public void resetCulture() { culture = 0; }

    public void setLegitimacy(int legitimacy) {
        this.legitimacy = Math.max(0, Math.min(100, legitimacy));
    }
    public void addLegitimacy(int amount) {
        setLegitimacy(legitimacy + amount);
    }
}
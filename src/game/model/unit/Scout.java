package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import game.model.world.World;
import javafx.scene.paint.Color;

public class Scout extends Unit {
    public Scout(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 20, 20, Color.rgb(255, 200, 50), 20, 5,
                4, 2, "Игрок", false, hexGrid, techTree);
        this.sightRadius = 5;
        this.squadMembers = 50;
        this.fatigue = 0;
        this.isResting = false;
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    protected void applyRiverCrossingPenalty(World world) {
        boolean hasRaft = false;
        if (techTree != null && techTree.isResearched("Примитивное плотничество")) {
            hasRaft = true;
        }
        int loss = hasRaft ? 2 : 10;
        squadMembers = Math.max(0, squadMembers - loss);
    }

    public void rest() { setFatigue(0); }
    public boolean isAlive() { return squadMembers > 0; }
}
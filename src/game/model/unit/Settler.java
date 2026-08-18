package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import game.model.world.World;
import javafx.scene.paint.Color;

public class Settler extends Unit {
    public Settler(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 30, 30, Color.rgb(66, 165, 245), 10, 2,
                3, 1, "Игрок", true, hexGrid, techTree);
        this.population = 500;
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
        int loss = hasRaft ? 10 : 50;
        if (population > 0) {
            population = Math.max(0, population - loss);
        }
    }
}
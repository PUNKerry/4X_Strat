package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import javafx.scene.paint.Color;

public class Horseman extends Unit {
    public Horseman(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 30, 30, Color.rgb(150, 100, 200), 35, 12,
                6, 2, "Игрок", false, hexGrid, techTree);
        this.sightRadius = 3;
        this.squadMembers = 30;
        this.fatigue = 0;
        this.isResting = false;
    }
    @Override
    public void update(double deltaTime) {}
}
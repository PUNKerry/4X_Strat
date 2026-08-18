package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import javafx.scene.paint.Color;

public class BatteringRam extends Unit {
    public BatteringRam(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 30, 30, Color.rgb(150, 80, 80), 60, 5,
                2, 2, "Игрок", false, hexGrid, techTree);
        this.sightRadius = 2;
        this.squadMembers = 20;
        this.fatigue = 0;
        this.isResting = false;
    }
    @Override
    public void update(double deltaTime) {}
}
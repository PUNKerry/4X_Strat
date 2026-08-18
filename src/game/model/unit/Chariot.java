package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import javafx.scene.paint.Color;

public class Chariot extends Unit {
    public Chariot(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 30, 30, Color.rgb(200, 180, 50), 40, 12,
                5, 2, "Игрок", false, hexGrid, techTree);
        this.sightRadius = 2;
        this.squadMembers = 30;
        this.fatigue = 0;
        this.isResting = false;
    }
    @Override
    public void update(double deltaTime) {}
}
package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import javafx.scene.paint.Color;

public class Galley extends Unit {
    public Galley(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 35, 25, Color.rgb(100, 150, 200), 40, 10,
                4, 2, "Игрок", false, hexGrid, techTree);
        this.sightRadius = 4;
        this.squadMembers = 60;
        this.fatigue = 0;
        this.isResting = false;
    }
    @Override
    public void update(double deltaTime) {}
}
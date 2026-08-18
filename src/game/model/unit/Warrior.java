package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import javafx.scene.paint.Color;

public class Warrior extends Unit {
    public Warrior(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 25, 25, Color.rgb(200, 150, 100), 30, 8,
                3, 2, "Игрок", false, hexGrid, techTree);
        this.sightRadius = 2;
        this.squadMembers = 50;
        this.fatigue = 0;
        this.isResting = false;
    }
    @Override
    public void update(double deltaTime) {}
}
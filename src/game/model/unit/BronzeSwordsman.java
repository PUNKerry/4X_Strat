package game.model.unit;

import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import javafx.scene.paint.Color;

public class BronzeSwordsman extends Unit {
    public BronzeSwordsman(Hex startHex, HexGrid hexGrid, TechTree techTree) {
        super(startHex, 25, 25, Color.rgb(180, 100, 50), 50, 15,
                3, 2, "Игрок", false, hexGrid, techTree);
        this.sightRadius = 2;
        this.squadMembers = 40;
        this.fatigue = 0;
        this.isResting = false;
    }
    @Override
    public void update(double deltaTime) {}
}
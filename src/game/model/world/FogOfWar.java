package game.model.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FogOfWar {
    public enum State {
        UNKNOWN,   // никогда не видели
        KNOWN,     // видели раньше, но сейчас не в зоне видимости
        VISIBLE    // видим сейчас
    }

    private final int cols;
    private final int rows;
    private final Map<Hex, State> states = new HashMap<>();

    public FogOfWar(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                states.put(new Hex(c, r), State.UNKNOWN);
            }
        }
    }

    public State getState(Hex hex) {
        return states.getOrDefault(hex, State.UNKNOWN);
    }

    public boolean isVisible(Hex hex) {
        return getState(hex) == State.VISIBLE;
    }

    public boolean isKnown(Hex hex) {
        State s = getState(hex);
        return s == State.VISIBLE || s == State.KNOWN;
    }

    public void updateVisibility(Set<Hex> visibleHexes) {
        // Все, кто был VISIBLE, становятся KNOWN
        for (Map.Entry<Hex, State> entry : states.entrySet()) {
            if (entry.getValue() == State.VISIBLE) {
                entry.setValue(State.KNOWN);
            }
        }
        // Теперь помечаем видимые
        for (Hex hex : visibleHexes) {
            if (hex.col >= 0 && hex.col < cols && hex.row >= 0 && hex.row < rows) {
                states.put(hex, State.VISIBLE);
            }
        }
    }

    public void reset() {
        for (Map.Entry<Hex, State> entry : states.entrySet()) {
            entry.setValue(State.UNKNOWN);
        }
    }
}
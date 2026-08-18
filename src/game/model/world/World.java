package game.model.world;

import engine.core.GameObject;
import engine.core.TileRenderer;
import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class World {
    private List<GameObject> objects = new ArrayList<>();
    private Comparator<GameObject> zComparator = Comparator.comparingInt(GameObject::getZIndex);
    private TileRenderer tileRenderer;

    public World() {}

    public void setTileRenderer(TileRenderer tileRenderer) {
        this.tileRenderer = tileRenderer;
    }

    public void addObject(GameObject obj) {
        objects.add(obj);
    }

    public void removeObject(GameObject obj) {
        objects.remove(obj);
    }

    public void update(double deltaTime) {
        for (GameObject obj : objects) {
            obj.update(deltaTime);
        }
    }

    public void render(GraphicsContext gc) {
        objects.sort(zComparator);
        for (GameObject obj : objects) {
            if (obj instanceof Tile && tileRenderer != null) {
                tileRenderer.render((Tile) obj, gc);
            } else {
                obj.render(gc);
            }
        }
    }

    public List<GameObject> getObjectsAt(double x, double y) {
        List<GameObject> result = new ArrayList<>();
        for (GameObject obj : objects) {
            if (x >= obj.getX() && x <= obj.getX() + obj.getWidth() &&
                    y >= obj.getY() && y <= obj.getY() + obj.getHeight()) {
                result.add(obj);
            }
        }
        return result;
    }

    public List<GameObject> getAllObjects() {
        return objects;
    }

    /**
     * Обновляет геометрию всех тайлов при изменении размера гекса.
     */
    public void updateTileGeometries(HexGrid hexGrid) {
        for (GameObject obj : objects) {
            if (obj instanceof Tile) {
                ((Tile) obj).updateGeometry(hexGrid);
            }
        }
    }
}
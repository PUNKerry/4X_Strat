package engine.infrastructure;

import engine.graphics.PathRenderer;
import game.controller.GameController;
import game.model.world.World;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameLoop extends AnimationTimer {
    private World world;
    private Canvas canvas;
    private GraphicsContext gc;
    private long lastUpdate = 0;
    private Runnable overlayRenderer;
    private GameController controller;
    private PathRenderer pathRenderer;

    public GameLoop(World world, Canvas canvas, GameController controller) {
        this.world = world;
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.controller = controller;
        this.pathRenderer = new PathRenderer(controller, controller.getCurrentHexGrid());
    }

    public void setOverlayRenderer(Runnable renderer) {
        this.overlayRenderer = renderer;
    }

    @Override
    public void handle(long now) {
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }
        double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
        lastUpdate = now;

        World currentWorld = controller.getCurrentWorld();
        if (currentWorld != null) {
            currentWorld.update(deltaTime);
        }

        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (currentWorld != null) {
            currentWorld.render(gc);
        }

        // Отрисовка пути через отдельный рендерер
        pathRenderer.render(gc);

        if (overlayRenderer != null) {
            overlayRenderer.run();
        }
    }
}
package engine.graphics;

import game.controller.GameController;
import game.model.unit.Unit;
import game.model.world.Hex;
import game.model.world.HexGrid;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class PathRenderer {

    private final GameController controller;
    private final HexGrid hexGrid;

    public PathRenderer(GameController controller, HexGrid hexGrid) {
        this.controller = controller;
        this.hexGrid = hexGrid;
    }

    public void render(GraphicsContext gc) {
        Unit selected = controller.getSelectedUnit();
        if (selected == null) return;

        // ================================================================
        // 1. ОТРИСОВКА АКТИВНОГО МАРШРУТА (уже подтверждённого)
        // ================================================================
        if (selected.isWaypointMode()) {
            List<Hex> waypoints = selected.getWaypoints();
            List<Hex> stopPoints = selected.getStopPoints();
            if (waypoints != null && !waypoints.isEmpty()) {
                // Жёлтый пунктирный путь
                gc.setStroke(Color.rgb(255, 215, 0, 0.9));
                gc.setLineWidth(4);
                gc.setLineDashes(12, 8);
                gc.setLineDashOffset(0);

                double[] first = hexGrid.hexToScreen(waypoints.get(0).col, waypoints.get(0).row);
                gc.beginPath();
                gc.moveTo(first[0], first[1]);
                for (int i = 1; i < waypoints.size(); i++) {
                    double[] pos = hexGrid.hexToScreen(waypoints.get(i).col, waypoints.get(i).row);
                    gc.lineTo(pos[0], pos[1]);
                }
                gc.stroke();
                gc.setLineDashes(null);
                gc.setLineWidth(1);

                // Точки остановки – оранжевые круги
                if (stopPoints != null && !stopPoints.isEmpty()) {
                    for (Hex stop : stopPoints) {
                        double[] pos = hexGrid.hexToScreen(stop.col, stop.row);
                        double size = hexGrid.getHexSize() * 0.2;
                        gc.setFill(Color.rgb(255, 165, 0, 0.7));
                        gc.fillOval(pos[0] - size/2, pos[1] - size/2, size, size);
                        gc.setStroke(Color.rgb(255, 140, 0, 0.9));
                        gc.setLineWidth(2);
                        gc.strokeOval(pos[0] - size/2, pos[1] - size/2, size, size);
                        gc.setLineWidth(1);
                    }
                }

                // Конечная цель – оранжевая подсветка
                Hex target = waypoints.get(waypoints.size() - 1);
                double[] targetPos = hexGrid.hexToScreen(target.col, target.row);
                double size = hexGrid.getHexSize();
                gc.setFill(Color.rgb(255, 165, 0, 0.3));
                gc.fillOval(targetPos[0] - size/2, targetPos[1] - size/2, size, size);
                gc.setStroke(Color.rgb(255, 165, 0, 0.9));
                gc.setLineWidth(3);
                gc.strokeOval(targetPos[0] - size/2, targetPos[1] - size/2, size, size);
                gc.setLineWidth(1);

                // Информация о маршруте
                int remaining = waypoints.size() - 1;
                gc.setFill(Color.WHITE);
                gc.setFont(javafx.scene.text.Font.font(13));
                gc.fillText("📌 Маршрут: " + remaining + " кл., остановок: " + stopPoints.size(),
                        targetPos[0] - 60, targetPos[1] + size/2 + 30);

                if (!selected.canMove()) {
                    gc.setFill(Color.rgb(255, 100, 100));
                    gc.setFont(javafx.scene.text.Font.font(12));
                    gc.fillText("⏳ Ожидание следующего хода...", targetPos[0] - 55, targetPos[1] + size/2 + 50);
                }
            }
        }

        // ================================================================
        // 2. ОТРИСОВКА ПУТИ ПРИ НАВЕДЕНИИ (если маршрут не активен)
        // ================================================================
        if (!selected.isWaypointMode()) {
            List<Hex> path = controller.getCurrentPath();
            List<Hex> stopPoints = controller.getCurrentStopPoints();
            if (path == null || path.isEmpty()) return;

            // Голубой пунктирный путь
            gc.setStroke(Color.rgb(100, 200, 255, 0.8));
            gc.setLineWidth(4);
            gc.setLineDashes(10, 8);
            gc.setLineDashOffset(0);

            double[] first = hexGrid.hexToScreen(path.get(0).col, path.get(0).row);
            gc.beginPath();
            gc.moveTo(first[0], first[1]);
            for (int i = 1; i < path.size(); i++) {
                double[] pos = hexGrid.hexToScreen(path.get(i).col, path.get(i).row);
                gc.lineTo(pos[0], pos[1]);
            }
            gc.stroke();
            gc.setLineDashes(null);
            gc.setLineWidth(1);

            // Точки остановки – зелёные круги
            if (stopPoints != null && !stopPoints.isEmpty()) {
                for (Hex stop : stopPoints) {
                    double[] pos = hexGrid.hexToScreen(stop.col, stop.row);
                    double size = hexGrid.getHexSize() * 0.2;
                    gc.setFill(Color.rgb(0, 255, 0, 0.5));
                    gc.fillOval(pos[0] - size/2, pos[1] - size/2, size, size);
                    gc.setStroke(Color.rgb(0, 200, 0, 0.8));
                    gc.setLineWidth(2);
                    gc.strokeOval(pos[0] - size/2, pos[1] - size/2, size, size);
                    gc.setLineWidth(1);
                }
            }

            // Целевая клетка – зелёная подсветка
            Hex target = path.get(path.size() - 1);
            double[] targetPos = hexGrid.hexToScreen(target.col, target.row);
            double size = hexGrid.getHexSize();
            gc.setFill(Color.rgb(0, 255, 0, 0.25));
            gc.fillOval(targetPos[0] - size/2, targetPos[1] - size/2, size, size);
            gc.setStroke(Color.rgb(0, 255, 0, 0.8));
            gc.setLineWidth(2);
            gc.strokeOval(targetPos[0] - size/2, targetPos[1] - size/2, size, size);
            gc.setLineWidth(1);

            // Информация о пути
            int steps = path.size() - 1;
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font(12));
            gc.fillText("Путь: " + steps + " кл., остановок: " + stopPoints.size(),
                    targetPos[0] - 40, targetPos[1] + size/2 + 20);

            // Если маршрут ожидает подтверждения – показываем подсказку
            if (controller.isWaypointPending()) {
                gc.setFill(Color.rgb(255, 255, 100, 0.9));
                gc.setFont(javafx.scene.text.Font.font(14));
                gc.fillText("✔ Подтвердить (клик)", targetPos[0] - 45, targetPos[1] + size/2 + 45);
            }
        }
    }
}
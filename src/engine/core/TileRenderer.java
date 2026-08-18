package engine.core;

import engine.graphics.SpriteManager;
import game.controller.GameController;
import game.model.city.City;
import game.model.city.District;
import game.model.world.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TileRenderer {

    private  HexGrid hexGrid;
    private final FogOfWar fogOfWar;
    private final GameController controller;
    private double canvasWidth = 0;
    private double canvasHeight = 0;

    public TileRenderer(HexGrid hexGrid, FogOfWar fogOfWar, GameController controller) {
        this.hexGrid = hexGrid;
        this.fogOfWar = fogOfWar;
        this.controller = controller;
    }

    public void setCanvasSize(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    public void setHexGrid(HexGrid hexGrid) {
        this.hexGrid = hexGrid;
    }

    public void render(Tile tile, GraphicsContext gc) {
        if (tile.getXPoints() == null || tile.getYPoints() == null) {
            tile.updateGeometry(hexGrid);
        }

        double cx = tile.getCenterX() + hexGrid.getOffsetX();
        double cy = tile.getCenterY() + hexGrid.getOffsetY();
        double[] xPoints = tile.getXPoints();
        double[] yPoints = tile.getYPoints();

        if (xPoints == null || yPoints == null) {
            return;
        }

        // Обрезка по видимости (если размер канваса известен)
        if (canvasWidth > 0 && canvasHeight > 0) {
            double halfSize = hexGrid.getHexSize();
            if (cx < -halfSize || cx > canvasWidth + halfSize || cy < -halfSize || cy > canvasHeight + halfSize) {
                return; // тайл вне видимой области
            }
        }

        double[] screenX = new double[6];
        double[] screenY = new double[6];
        for (int i = 0; i < 6; i++) {
            screenX[i] = cx + xPoints[i];
            screenY[i] = cy + yPoints[i];
        }

        Hex hex = tile.getHex();
        boolean cityView = controller != null && controller.isCityView();
        FogOfWar.State fogState = FogOfWar.State.VISIBLE;
        if (!cityView && fogOfWar != null) {
            fogState = fogOfWar.getState(hex);
        }

        if (cityView) {
            City centerCity = controller != null ? controller.getZoomedCity() : null;
            if (centerCity != null) {
                Hex centerHex = centerCity.getCenter();
                if (centerHex.distanceTo(hex) > 6) {
                    gc.setFill(Color.BLACK);
                    gc.fillPolygon(screenX, screenY, 6);
                    return;
                }
            }
        }

        if (fogState == FogOfWar.State.UNKNOWN && !cityView) {
            gc.setFill(Color.BLACK);
            gc.fillPolygon(screenX, screenY, 6);
            return;
        }

        // ---- Спрайт местности ----
        Image sprite = tile.getSprite();
        if (sprite != null) {
            double size = hexGrid.getHexSize();
            double x = cx - size;
            double y = cy - size;
            double w = 2 * size;
            double h = 2 * size;
            gc.drawImage(sprite, x, y, w, h);
        } else {
            gc.setFill(tile.getTerrain().getColor());
            gc.fillPolygon(screenX, screenY, 6);
        }

        // Сетка
        gc.setStroke(Color.rgb(128, 128, 128, 0.3));
        gc.setLineWidth(1.5);
        gc.strokePolygon(screenX, screenY, 6);

        // Затемнение вне города
        if (cityView) {
            City currentCity = controller != null ? controller.getZoomedCity() : null;
            if (currentCity != null && !currentCity.getTiles().contains(hex)) {
                gc.setFill(Color.rgb(0, 0, 0, 0.6));
                gc.fillPolygon(screenX, screenY, 6);
            }
        }

        // Подсветка
        if (tile.isHighlighted()) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(4);
            gc.strokePolygon(screenX, screenY, 6);
            gc.setLineWidth(1);
        }

        // Заливка территории города
        if (tile.isCityTile()) {
            gc.setFill(Color.rgb(0, 150, 255, 0.2));
            gc.fillPolygon(screenX, screenY, 6);
        }

        // Центр города
        if (tile.isCityCenter()) {
            double size = hexGrid.getHexSize();
            gc.setFill(Color.rgb(255, 215, 0, 0.7));
            gc.fillOval(cx - size * 0.25, cy - size * 0.25, size * 0.5, size * 0.5);
            gc.setStroke(Color.rgb(200, 150, 0));
            gc.setLineWidth(2);
            gc.strokeOval(cx - size * 0.25, cy - size * 0.25, size * 0.5, size * 0.5);
            gc.setLineWidth(1);
        }

        // Граница города
        if (tile.isCityTile() && tile.getCityTiles() != null && (fogState != FogOfWar.State.UNKNOWN || cityView)) {
            int[][] edgeVertexIndices = {
                    {5, 0}, {4, 5}, {3, 4}, {2, 3}, {1, 2}, {0, 1}
            };
            Hex[] neighbors = hex.neighbors();
            for (int i = 0; i < 6; i++) {
                Hex neighbor = neighbors[i];
                if (!tile.getCityTiles().contains(neighbor)) {
                    int v1 = edgeVertexIndices[i][0];
                    int v2 = edgeVertexIndices[i][1];
                    gc.setStroke(Color.rgb(0, 150, 255, 0.9));
                    gc.setLineWidth(3);
                    gc.setLineDashes(5, 5);
                    gc.strokeLine(screenX[v1], screenY[v1], screenX[v2], screenY[v2]);
                    gc.setLineDashes(null);
                }
            }
        }

        // --- Назначение горожан ---
        if (controller != null && controller.isAssignmentMode()) {
            City city = controller.getAssignmentCity();
            if (city != null && city.getTiles().contains(hex)) {
                int assigned = city.getAssignedCount(hex);
                int required = city.getRequiredWorkers(hex);
                double size = hexGrid.getHexSize();
                if (assigned > 0) {
                    gc.setFill(Color.rgb(0, 255, 0, 0.6));
                    gc.fillOval(cx - size * 0.15, cy - size * 0.15, size * 0.3, size * 0.3);
                    gc.setFill(Color.BLACK);
                    gc.fillText(String.valueOf(assigned), cx - size * 0.05, cy + size * 0.05);
                } else if (!hex.equals(city.getCenter())) {
                    gc.setStroke(Color.rgb(255, 255, 0, 0.5));
                    gc.setLineWidth(2);
                    gc.strokeOval(cx - size * 0.2, cy - size * 0.2, size * 0.4, size * 0.4);
                    gc.setLineWidth(1);
                }
                if (required > 1) {
                    gc.setFill(Color.WHITE);
                    gc.fillText("+" + required, cx + size * 0.2, cy - size * 0.2);
                }
            }
        }

        // --- Улучшения ---
        // --- Улучшения ---
        Improvement improvement = tile.getImprovement();
        if (improvement != null && (fogState != FogOfWar.State.UNKNOWN || cityView)) {
            double size = hexGrid.getHexSize();
            if (improvement.isUnderConstruction()) {
                gc.setFill(Color.rgb(255, 200, 0, 0.3));
                gc.fillOval(cx - size * 0.3, cy - size * 0.3, size * 0.6, size * 0.6);
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(2);
                gc.strokeOval(cx - size * 0.3, cy - size * 0.3, size * 0.6, size * 0.6);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(10));
                gc.fillText("Стройка", cx - size * 0.25, cy + size * 0.05);
            } else {
                // Существующие улучшения
                if (improvement.getType() == Improvement.Type.FARM) {
                    gc.setFill(Color.rgb(34, 139, 34, 0.8));
                    gc.fillRect(cx - size * 0.2, cy - size * 0.2, size * 0.4, size * 0.4);
                } else if (improvement.getType() == Improvement.Type.MINE) {
                    gc.setFill(Color.rgb(128, 128, 128, 0.8));
                    gc.fillPolygon(new double[]{cx, cx - size * 0.25, cx + size * 0.25},
                            new double[]{cy - size * 0.3, cy + size * 0.2, cy + size * 0.2}, 3);
                } else if (improvement.getType() == Improvement.Type.PASTURE) {
                    gc.setFill(Color.rgb(34, 139, 34, 0.6));
                    gc.fillRect(cx - size * 0.25, cy - size * 0.25, size * 0.5, size * 0.5);
                    gc.setStroke(Color.rgb(0, 100, 0));
                    gc.setLineWidth(1.5);
                    gc.strokeRect(cx - size * 0.25, cy - size * 0.25, size * 0.5, size * 0.5);
                } else if (improvement.getType() == Improvement.Type.LUMBERMILL) {
                    gc.setFill(Color.rgb(139, 69, 19, 0.7));
                    gc.fillRect(cx - size * 0.2, cy - size * 0.3, size * 0.4, size * 0.6);
                    gc.setStroke(Color.rgb(100, 50, 0));
                    gc.setLineWidth(1.5);
                    gc.strokeRect(cx - size * 0.2, cy - size * 0.3, size * 0.4, size * 0.6);
                } else if (improvement.getType() == Improvement.Type.QUARRY) {
                    gc.setFill(Color.rgb(128, 128, 128, 0.8));
                    gc.fillOval(cx - size * 0.25, cy - size * 0.25, size * 0.5, size * 0.5);
                    gc.setStroke(Color.rgb(80, 80, 80));
                    gc.setLineWidth(1.5);
                    gc.strokeOval(cx - size * 0.25, cy - size * 0.25, size * 0.5, size * 0.5);
                }
            }
        }

        // --- Районы ---
        District district = tile.getDistrict();
        if (district != null && (fogState != FogOfWar.State.UNKNOWN || cityView)) {
            Image districtSprite = null;
            switch (district.getType()) {
                case HOUSING:
                    districtSprite = SpriteManager.getInstance().getSprite("districts/housing.png");
                    break;
                case DISTRICT_1:
                    districtSprite = SpriteManager.getInstance().getSprite("districts/district1.png");
                    break;
                case DISTRICT_2:
                    districtSprite = SpriteManager.getInstance().getSprite("districts/district2.png");
                    break;
            }
            double size = hexGrid.getHexSize();
            if (districtSprite != null) {
                double x = cx - size * 0.35;
                double y = cy - size * 0.35;
                double w = size * 0.7;
                double h = size * 0.7;
                gc.drawImage(districtSprite, x, y, w, h);
            } else {
                gc.setFill(Color.rgb(255, 255, 255, 0.1));
                gc.fillOval(cx - size * 0.35, cy - size * 0.35, size * 0.7, size * 0.7);
                gc.setStroke(Color.rgb(200, 200, 255, 0.3));
                gc.setLineWidth(1);
                gc.strokeOval(cx - size * 0.35, cy - size * 0.35, size * 0.7, size * 0.7);
                gc.setFont(Font.font(size * 0.6));
                gc.setFill(Color.WHITE);
                String symbol;
                switch (district.getType()) {
                    case HOUSING:
                        symbol = "🏠";
                        break;
                    case DISTRICT_1:
                        symbol = "⚗️";
                        break;
                    case DISTRICT_2:
                        symbol = "🎵";
                        break;
                    default:
                        symbol = "⬜";
                }
                gc.fillText(symbol, cx - size * 0.2, cy + size * 0.2);
            }
        }

        // --- Туман KNOWN ---
        if (!cityView && fogState == FogOfWar.State.KNOWN) {
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.fillPolygon(screenX, screenY, 6);
        }
    }
}
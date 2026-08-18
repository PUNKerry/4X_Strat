package game.model.world;

public class HexGrid {
    private double hexSize;
    private double offsetX = 0;
    private double offsetY = 0;
    private double minOffsetX, maxOffsetX, minOffsetY, maxOffsetY;
    private boolean boundsEnabled = true;
    private double padding = 150;

    public HexGrid(double hexSize) {
        this.hexSize = hexSize;
        minOffsetX = -10000;
        maxOffsetX = 10000;
        minOffsetY = -10000;
        maxOffsetY = 10000;
    }

    public void setHexSize(double newSize) {
        this.hexSize = newSize;
    }

    public void setOffset(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        clampOffset();
    }

    public void setOffsetSilent(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setWorldBounds(double mapWidth, double mapHeight, double canvasWidth, double canvasHeight) {
        double effectiveWidth = mapWidth + 2 * padding;
        double effectiveHeight = mapHeight + 2 * padding;

        if (effectiveWidth <= canvasWidth) {
            double centerOffset = (canvasWidth - mapWidth) / 2;
            minOffsetX = centerOffset;
            maxOffsetX = centerOffset;
        } else {
            minOffsetX = canvasWidth - mapWidth - padding;
            maxOffsetX = padding;
        }
        if (effectiveHeight <= canvasHeight) {
            double centerOffset = (canvasHeight - mapHeight) / 2;
            minOffsetY = centerOffset;
            maxOffsetY = centerOffset;
        } else {
            minOffsetY = canvasHeight - mapHeight - padding;
            maxOffsetY = padding;
        }
    }

    private void clampOffset() {
        if (!boundsEnabled) return;
        offsetX = Math.min(maxOffsetX, Math.max(minOffsetX, offsetX));
        offsetY = Math.min(maxOffsetY, Math.max(minOffsetY, offsetY));
    }

    public void moveCamera(double dx, double dy) {
        offsetX += dx;
        offsetY += dy;
        clampOffset();
    }

    public double[] hexToScreen(int col, int row) {
        double x = hexSize * (Math.sqrt(3) * col + Math.sqrt(3) / 2 * (row % 2));
        double y = hexSize * (1.5 * row);
        return new double[]{x + offsetX, y + offsetY};
    }

    // ИСПРАВЛЕНО: теперь формула совпадает с hexToScreen без смещения
    public double[] hexToWorld(int col, int row) {
        double x = hexSize * (Math.sqrt(3) * col + Math.sqrt(3) / 2 * (row % 2));
        double y = hexSize * (1.5 * row);
        return new double[]{x, y};
    }

    public Hex screenToHex(double x, double y) {
        double xOff = x - offsetX;
        double yOff = y - offsetY;

        double row = yOff / (hexSize * 1.5);
        double r = Math.round(row);
        double col = (xOff / hexSize) / Math.sqrt(3) + 0.5 * (r % 2);
        double c = Math.round(col);

        Hex best = new Hex((int)c, (int)r);
        double[] bestCenter = hexToScreen(best.col, best.row);
        double bestDist = distance(x, y, bestCenter[0], bestCenter[1]);

        for (Hex neighbor : best.neighbors()) {
            double[] center = hexToScreen(neighbor.col, neighbor.row);
            double d = distance(x, y, center[0], center[1]);
            if (d < bestDist) {
                bestDist = d;
                best = neighbor;
            }
        }
        return best;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx*dx + dy*dy);
    }

    public double getHexWidth() { return hexSize * 2; }
    public double getHexHeight() { return Math.sqrt(3) * hexSize; }
    public double getHexSize() { return hexSize; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public void setBoundsEnabled(boolean enabled) { this.boundsEnabled = enabled; }
    public void setPadding(double padding) { this.padding = padding; }
}
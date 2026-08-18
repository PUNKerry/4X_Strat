package game.controller;

import game.model.unit.Unit;
import game.model.world.Hex;
import game.model.world.HexGrid;

public class CameraManager {

    private final GameController controller;

    private double currentHexSize;
    private final double MIN_HEX_SIZE;
    private final double MAX_HEX_SIZE;

    private double canvasWidth = 800;
    private double canvasHeight = 600;

    private double savedOffsetX = 0;
    private double savedOffsetY = 0;
    private double savedHexSize = 28;

    public CameraManager(GameController controller, double initialHexSize, double minHexSize, double maxHexSize) {
        this.controller = controller;
        this.currentHexSize = initialHexSize;
        this.MIN_HEX_SIZE = minHexSize;
        this.MAX_HEX_SIZE = maxHexSize;
    }

    public double getCurrentHexSize() { return currentHexSize; }
    public void setCurrentHexSize(double size) {
        double newSize = Math.min(MAX_HEX_SIZE, Math.max(MIN_HEX_SIZE, size));
        if (newSize != currentHexSize) {
            currentHexSize = newSize;
            controller.getHexGrid().setHexSize(currentHexSize);
            controller.getWorld().updateTileGeometries(controller.getHexGrid());
            if (!controller.isCityView()) {
                updateWorldBounds();
            }
        }
    }

    public double getSavedOffsetX() { return savedOffsetX; }
    public void setSavedOffsetX(double x) { savedOffsetX = x; }
    public double getSavedOffsetY() { return savedOffsetY; }
    public void setSavedOffsetY(double y) { savedOffsetY = y; }
    public double getSavedHexSize() { return savedHexSize; }
    public void setSavedHexSize(double size) { savedHexSize = size; }

    public void setCanvasSize(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    public void moveCamera(double dx, double dy) {
        getHexGrid().moveCamera(dx, dy);
    }

    public void zoomCamera(double factor, double mouseX, double mouseY) {
        HexGrid hexGrid = getHexGrid();
        double newSize = currentHexSize * factor;
        if (newSize < MIN_HEX_SIZE || newSize > MAX_HEX_SIZE) return;

        Hex centerHex = hexGrid.screenToHex(mouseX, mouseY);
        currentHexSize = newSize;
        hexGrid.setHexSize(currentHexSize);
        controller.getWorld().updateTileGeometries(hexGrid);
        if (!controller.isCityView()) updateWorldBounds();

        double[] worldPos = hexGrid.hexToWorld(centerHex.col, centerHex.row);
        double newOffsetX = mouseX - worldPos[0];
        double newOffsetY = mouseY - worldPos[1];

        hexGrid.setBoundsEnabled(false);
        hexGrid.setOffset(newOffsetX, newOffsetY);
        hexGrid.setBoundsEnabled(true);
    }

    public void updateWorldBounds() {
        if (controller.isCityView()) return;
        HexGrid hexGrid = getHexGrid();
        int cols = controller.getCols();
        int rows = controller.getRows();
        double[] topLeft = hexGrid.hexToScreen(0, 0);
        double[] bottomRight = hexGrid.hexToScreen(cols - 1, rows - 1);
        double mapWidth = bottomRight[0] - topLeft[0] + hexGrid.getHexSize() * 2;
        double mapHeight = bottomRight[1] - topLeft[1] + hexGrid.getHexSize() * 2;
        hexGrid.setWorldBounds(mapWidth, mapHeight, canvasWidth, canvasHeight);
    }

    public void updateWorldBoundsFromCanvas(double canvasWidth, double canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        if (!controller.isCityView()) {
            HexGrid hexGrid = getHexGrid();
            int cols = controller.getCols();
            int rows = controller.getRows();
            double[] topLeft = hexGrid.hexToScreen(0, 0);
            double[] bottomRight = hexGrid.hexToScreen(cols - 1, rows - 1);
            double mapWidth = bottomRight[0] - topLeft[0] + hexGrid.getHexSize() * 2;
            double mapHeight = bottomRight[1] - topLeft[1] + hexGrid.getHexSize() * 2;
            hexGrid.setWorldBounds(mapWidth, mapHeight, canvasWidth, canvasHeight);
        }
    }

    public void recalculateHexSize(double canvasWidth, double canvasHeight) {
        if (canvasWidth <= 0 || canvasHeight <= 0) return;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        if (!controller.isCityView()) {
            HexGrid hexGrid = getHexGrid();
            int cols = controller.getCols();
            int rows = controller.getRows();
            double sizeFromWidth = (canvasWidth * 0.85) / (cols * 0.85);
            double sizeFromHeight = (canvasHeight * 0.85) / (rows * 0.85);
            double optimalSize = Math.min(sizeFromWidth, sizeFromHeight);
            currentHexSize = Math.min(MAX_HEX_SIZE, Math.max(MIN_HEX_SIZE, optimalSize));
            hexGrid.setHexSize(currentHexSize);
            controller.getWorld().updateTileGeometries(hexGrid);
            updateWorldBoundsFromCanvas(canvasWidth, canvasHeight);
        }
    }

    public void centerMap() {
        HexGrid hexGrid = getHexGrid();
        if (hexGrid == null) return;
        int cols = controller.getCols();
        int rows = controller.getRows();
        double[] topLeft = hexGrid.hexToScreen(0, 0);
        double[] bottomRight = hexGrid.hexToScreen(cols - 1, rows - 1);
        double mapWidth = bottomRight[0] - topLeft[0] + currentHexSize * 2;
        double mapHeight = bottomRight[1] - topLeft[1] + currentHexSize * 2;
        double initOffsetX = (canvasWidth - mapWidth) / 2 - topLeft[0];
        double initOffsetY = (canvasHeight - mapHeight) / 2 - topLeft[1];
        hexGrid.setOffset(initOffsetX, initOffsetY);
    }

    public void centerOnUnit(Unit unit) {
        if (unit == null || unit.getCurrentHex() == null) return;
        HexGrid hexGrid = getHexGrid();
        double[] center = hexGrid.hexToScreen(unit.getCurrentHex().col, unit.getCurrentHex().row);
        double targetX = canvasWidth / 2 - center[0];
        double targetY = canvasHeight / 2 - center[1];
        hexGrid.moveCamera(targetX - hexGrid.getOffsetX(), targetY - hexGrid.getOffsetY());
    }

    public void centerOnHex(Hex hex) {
        if (hex == null) return;
        HexGrid hexGrid = getHexGrid();
        double[] worldPos = hexGrid.hexToWorld(hex.col, hex.row);
        double targetX = canvasWidth / 2 - worldPos[0];
        double targetY = canvasHeight / 2 - worldPos[1];
        hexGrid.setOffset(targetX, targetY);
    }

    public void saveCameraState() {
        HexGrid hexGrid = getHexGrid();
        savedOffsetX = hexGrid.getOffsetX();
        savedOffsetY = hexGrid.getOffsetY();
        savedHexSize = currentHexSize;
    }

    public void restoreCameraState() {
        HexGrid hexGrid = getHexGrid();
        hexGrid.setHexSize(savedHexSize);
        hexGrid.setOffset(savedOffsetX, savedOffsetY);
        currentHexSize = savedHexSize;
        controller.getWorld().updateTileGeometries(hexGrid);
    }

    public double calculateHexSizeForCityView(int radius) {
        int localCols = 2 * radius + 1;
        int localRows = 2 * radius + 1;
        double visibleWidth = canvasWidth * 0.95;
        double visibleHeight = canvasHeight * 0.95;
        double sizeFromWidth = visibleWidth / (localCols * Math.sqrt(3));
        double sizeFromHeight = visibleHeight / (localRows * 1.5);
        double target = Math.min(sizeFromWidth, sizeFromHeight);
        return Math.min(MAX_HEX_SIZE, Math.max(MIN_HEX_SIZE, target));
    }

    private HexGrid getHexGrid() {
        return controller.getHexGrid();
    }
}
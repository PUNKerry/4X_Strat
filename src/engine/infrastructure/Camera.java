package engine.infrastructure;

public class Camera {
    private double x, y; // смещение (offset)
    private double zoom; // масштаб (1.0 = 100%)
    private double minZoom, maxZoom;
    private double minX, maxX, minY, maxY; // границы

    public Camera() {
        this(0, 0, 1.0);
    }

    public Camera(double x, double y, double zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.minZoom = 0.5;
        this.maxZoom = 2.0;
        setBounds(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public void setBounds(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        clamp();
    }

    public void move(double dx, double dy) {
        x += dx;
        y += dy;
        clamp();
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        clamp();
    }

    public void zoom(double factor, double centerX, double centerY) {
        double newZoom = zoom * factor;
        if (newZoom < minZoom) newZoom = minZoom;
        if (newZoom > maxZoom) newZoom = maxZoom;
        if (newZoom == zoom) return;
        // Масштабируем относительно точки (centerX, centerY) на экране
        // Для простоты будем менять zoom, а смещение корректировать позже
        zoom = newZoom;
        // Чтобы сохранить точку под курсором, нужно пересчитать смещение
        // Мы это сделаем в отдельном методе, но для простоты оставим так.
        clamp();
    }

    public void setZoom(double zoom) {
        this.zoom = Math.min(maxZoom, Math.max(minZoom, zoom));
        clamp();
    }

    private void clamp() {
        // Ограничиваем смещение, чтобы камера не выходила за границы
        if (x < minX) x = minX;
        if (x > maxX) x = maxX;
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;
    }

    // Преобразование мировых координат в экранные
    public double worldToScreenX(double worldX) {
        return (worldX - x) * zoom;
    }

    public double worldToScreenY(double worldY) {
        return (worldY - y) * zoom;
    }

    // Преобразование экранных координат в мировые
    public double screenToWorldX(double screenX) {
        return screenX / zoom + x;
    }

    public double screenToWorldY(double screenY) {
        return screenY / zoom + y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZoom() { return zoom; }
    public double getMinZoom() { return minZoom; }
    public double getMaxZoom() { return maxZoom; }
    public void setMinZoom(double minZoom) { this.minZoom = minZoom; }
    public void setMaxZoom(double maxZoom) { this.maxZoom = maxZoom; }
}
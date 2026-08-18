package engine.core;

import engine.contract.Renderable;
import engine.contract.Updatable;

public abstract class GameObject implements Renderable, Updatable {
    protected double x, y;
    protected double width, height;
    protected int zIndex = 0;

    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getZIndex() { return zIndex; }
    public void setZIndex(int zIndex) { this.zIndex = zIndex; }
}
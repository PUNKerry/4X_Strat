package game.model.world;

import engine.core.GameObject;
import game.model.city.District;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Set;

public class Tile extends GameObject {
    private final Hex hex;
    private final TerrainType terrain;
    private Image sprite;
    private Color currentColor;
    private boolean highlighted = false;
    private boolean isCityTile = false;
    private boolean isCityCenter = false;
    private String cityName = "";
    private Set<Hex> cityTiles = null;
    private Improvement improvement;
    private District district;

    // Кэшированная геометрия (мировые координаты без смещения)
    private double centerX;
    private double centerY;
    private double[] xPoints;
    private double[] yPoints;

    public Tile(Hex hex, TerrainType terrain, Image sprite) {
        super(0, 0, 0, 0);
        this.hex = hex;
        this.terrain = terrain;
        this.sprite = sprite;
        this.currentColor = terrain.getColor();
        setZIndex(-1);
        // Геометрия будет обновлена позже через updateGeometry()
    }

    public void updateGeometry(HexGrid hexGrid) {
        double[] pos = hexGrid.hexToWorld(hex.col, hex.row);
        this.centerX = pos[0];
        this.centerY = pos[1];
        double size = hexGrid.getHexSize();
        xPoints = new double[6];
        yPoints = new double[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(30 + 60 * i);
            xPoints[i] = size * Math.cos(angle);
            yPoints[i] = size * Math.sin(angle);
        }
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void render(GraphicsContext gc) {
        // Рендеринг делегирован во внешний TileRenderer
    }

    // --- Геттеры ---
    public Hex getHex() { return hex; }
    public TerrainType getTerrain() { return terrain; }
    public Image getSprite() { return sprite; }
    public void setSprite(Image sprite) { this.sprite = sprite; }
    public boolean isHighlighted() { return highlighted; }
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        this.currentColor = highlighted ? Color.rgb(255, 255, 0, 0.3) : terrain.getColor();
    }
    public boolean isCityTile() { return isCityTile; }
    public void setCityTile(boolean isCityTile) {
        this.isCityTile = isCityTile;
        if (!isCityTile) {
            this.cityTiles = null;
            this.isCityCenter = false;
        }
    }
    public boolean isCityCenter() { return isCityCenter; }
    public void setCityCenter(boolean isCityCenter) { this.isCityCenter = isCityCenter; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public Set<Hex> getCityTiles() { return cityTiles; }
    public void setCityTiles(Set<Hex> cityTiles) {
        this.cityTiles = cityTiles;
        this.isCityTile = (cityTiles != null && cityTiles.contains(hex));
    }
    public Improvement getImprovement() { return improvement; }
    public void setImprovement(Improvement improvement) { this.improvement = improvement; }
    public District getDistrict() { return district; }
    public void setDistrict(District district) { this.district = district; }

    // Геттеры для кэшированной геометрии
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double[] getXPoints() { return xPoints; }
    public double[] getYPoints() { return yPoints; }
}
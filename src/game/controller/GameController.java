package game.controller;

import engine.core.GameObject;
import engine.core.TileRenderer;
import engine.infrastructure.GameState;
import engine.infrastructure.HistoryTracker;
import game.UI.GovernmentPanel;
import game.UI.UIManager;
import game.model.city.City;
import game.model.city.CityGlobal;
import game.model.city.District;
import game.model.registry.*;
import game.model.research.TechNode;
import game.model.research.TechTree;
import game.model.unit.Settler;
import game.model.unit.Unit;
import game.model.world.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameController {

    // --- Основные компоненты ---
    private World world;
    private HexGrid hexGrid;
    private GameState gameState;
    private TechTree techTree;
    private Advisor advisor;
    private UIManager uiManager;

    // --- Регистры ---
    private final TechRegistry techRegistry;
    private final ImprovementRegistry improvementRegistry;
    private final DistrictRegistry districtRegistry;
    private final CenterImprovementRegistry centerImprovementRegistry;

    // --- Менеджеры ---
    private final TurnManager turnManager;
    private final SelectionManager selectionManager;
    private final PlacementManager placementManager;
    private final FogManager fogManager;
    private final UnitManager unitManager;
    private final CameraManager cameraManager;
    private final CityViewManager cityViewManager;
    private final CityManager cityManager;
    private final AssignmentManager assignmentManager;
    private final GovernmentManager governmentManager;

    // --- Панель правительства ---
    private final GovernmentPanel governmentPanel;

    // --- Дополнительные компоненты ---
    private final FogOfWar fogOfWar;
    private TileRenderer tileRenderer;

    // --- История ---
    private HistoryTracker historyTracker = new HistoryTracker();

    // --- Состояние игры ---
    private final int cols = 60;
    private final int rows = 40;
    private double canvasWidth = 800;
    private double canvasHeight = 600;

    // Колбэки UI
    Runnable onUnitSelected;
    Runnable onCitySelected;
    Runnable onResourcesUpdated;
    Runnable onProgressUpdated;
    Runnable onStatusChanged;

    // Визуализация пути
    private List<Hex> currentPath = new ArrayList<>();
    private List<Hex> currentStopPoints = new ArrayList<>();
    private boolean isWaypointPending = false;
    private Hex pendingTargetHex = null;

    // ========================================================================
    // Конструктор
    // ========================================================================

    public GameController(World world, HexGrid hexGrid, GameState gameState, TechTree techTree) {
        this.world = world;
        this.hexGrid = hexGrid;
        this.gameState = gameState;
        this.techTree = techTree;

        // Создаём регистры на основе переданного TechTree
        this.techRegistry = new TechRegistry(techTree);
        this.improvementRegistry = new ImprovementRegistry();
        this.districtRegistry = new DistrictRegistry();
        this.centerImprovementRegistry = new CenterImprovementRegistry();

        this.fogOfWar = new FogOfWar(cols, rows);
        this.tileRenderer = new TileRenderer(hexGrid, fogOfWar, this);
        world.setTileRenderer(tileRenderer);

        this.turnManager = new TurnManager(this);
        this.selectionManager = new SelectionManager(this);
        this.placementManager = new PlacementManager(this);
        this.fogManager = new FogManager(this, fogOfWar);
        this.unitManager = new UnitManager(this, techTree);
        this.cameraManager = new CameraManager(this, 28, 12, 55);
        this.cityViewManager = new CityViewManager(this, cameraManager);
        this.cityManager = new CityManager(this);
        this.assignmentManager = new AssignmentManager(this);
        this.governmentManager = new GovernmentManager(this, techRegistry);
        this.governmentPanel = new GovernmentPanel(this);

        CityGlobal.setCities(cityManager.getCities());

        world.updateTileGeometries(hexGrid);
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public World getCurrentWorld() { return world; }
    public HexGrid getCurrentHexGrid() { return hexGrid; }
    public World getWorld() { return world; }
    public HexGrid getHexGrid() { return hexGrid; }
    public GameState getGameState() { return gameState; }
    public TechTree getTechTree() { return techTree; }
    public Advisor getAdvisor() { return advisor; }
    public UIManager getUIManager() { return uiManager; }
    public FogOfWar getFogOfWar() { return fogOfWar; }
    public TileRenderer getTileRenderer() { return tileRenderer; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }

    // Геттеры для регистров
    public TechRegistry getTechRegistry() { return techRegistry; }
    public ImprovementRegistry getImprovementRegistry() { return improvementRegistry; }
    public DistrictRegistry getDistrictRegistry() { return districtRegistry; }
    public CenterImprovementRegistry getCenterImprovementRegistry() { return centerImprovementRegistry; }

    public int getScience() { return gameState.getScience(); }
    public int getCulture() { return gameState.getCulture(); }
    public int getTreasury() { return gameState.getTreasury(); }
    public int getPiety() { return gameState.getPiety(); }

    public List<Hex> getCurrentPath() { return currentPath; }
    public List<Hex> getCurrentStopPoints() { return currentStopPoints; }
    public boolean isWaypointPending() { return isWaypointPending; }

    // Геттеры для правительства
    public GovernmentManager getGovernmentManager() { return governmentManager; }
    public GovernmentPanel getGovernmentPanel() { return governmentPanel; }
    public boolean isGovernmentUnlocked() {
        return techTree.isResearched("Вождество");
    }

    // ========================================================================
    // Геттеры, делегированные менеджерам
    // ========================================================================

    public int getTurnNumber() { return turnManager.getTurnNumber(); }
    public boolean isPlayerTurn() { return turnManager.isPlayerTurn(); }
    public TechNode getCurrentTech() { return turnManager.getCurrentTech(); }
    public TechNode getCurrentSocial() { return turnManager.getCurrentSocial(); }
    public TechNode getCurrentReligion() { return turnManager.getCurrentReligion(); }
    public int getTechInvested() { return turnManager.getTechInvested(); }
    public int getSocialInvested() { return turnManager.getSocialInvested(); }
    public int getReligionInvested() { return turnManager.getReligionInvested(); }
    public int getSciencePerTurn() { return turnManager.getSciencePerTurn(); }
    public int getCulturePerTurn() { return turnManager.getCulturePerTurn(); }
    public int getFaithPerTurn() { return turnManager.getFaithPerTurn(); }
    public int getTechCost() { return turnManager.getTechCost(); }
    public int getSocialCost() { return turnManager.getSocialCost(); }
    public int getReligionCost() { return turnManager.getReligionCost(); }
    public int getTechTurnsLeft() { return turnManager.getTechTurnsLeft(); }
    public int getSocialTurnsLeft() { return turnManager.getSocialTurnsLeft(); }
    public int getReligionTurnsLeft() { return turnManager.getReligionTurnsLeft(); }
    public double getTechProgress() { return turnManager.getTechProgress(); }
    public double getSocialProgress() { return turnManager.getSocialProgress(); }
    public double getReligionProgress() { return turnManager.getReligionProgress(); }
    public double[] computePoleValues() { return turnManager.computePoleValues(); }
    public boolean isReligionUnlocked() { return techTree.isReligionUnlocked(); }
    public boolean isLegitimacyUnlocked() { return techTree.isLegitimacyUnlocked(); }
    public boolean isMoneyUnlocked() { return techTree.isMoneyUnlocked(); }
    public boolean selectTech(String techName) { return turnManager.selectTech(techName); }
    public boolean selectSocial(String socialName) { return turnManager.selectSocial(socialName); }
    public boolean selectReligion(String religionName) { return turnManager.selectReligion(religionName); }

    public TurnManager getTurnManager() { return turnManager; }

    // SelectionManager
    public Unit getSelectedUnit() { return selectionManager.getSelectedUnit(); }
    public City getSelectedCity() { return selectionManager.getSelectedCity(); }
    public Map<Hex, Integer> getReachableHexes() { return selectionManager.getReachableHexes(); }
    public void selectUnit(Unit unit) {
        selectionManager.selectUnit(unit);
        clearPath();
        if (onUnitSelected != null) onUnitSelected.run();
    }
    public void selectCity(City city) {
        selectionManager.selectCity(city);
        clearPath();
        if (onCitySelected != null) onCitySelected.run();
    }
    public void highlightAvailableMoves(Unit unit) { selectionManager.highlightAvailableMoves(unit); }
    public void clearHighlights() {
        selectionManager.clearHighlights();
        clearPath();
    }

    // PlacementManager
    public boolean isPlacementMode() { return placementManager.isPlacementMode(); }
    public boolean isDistrictPlacementMode() { return placementManager.isDistrictPlacementMode(); }
    public void enterPlacementMode(City city, Improvement.Type type) { placementManager.enterPlacementMode(city, type); }
    public void exitPlacementMode() { placementManager.exitPlacementMode(); }
    public void handlePlacementClick(Hex hex) { placementManager.handlePlacementClick(hex); }
    public void enterDistrictPlacementMode(City city, District.Type type) { placementManager.enterDistrictPlacementMode(city, type); }
    public void exitDistrictPlacementMode() { placementManager.exitDistrictPlacementMode(); }
    public void handleDistrictPlacementClick(Hex hex) { placementManager.handleDistrictPlacementClick(hex); }

    // FogManager
    public void recalculateFog() { fogManager.recalculateFog(); }

    // UnitManager
    public List<Unit> getAllUnits() { return unitManager.getAllUnits(); }
    public Unit getPlayerUnit() { return unitManager.getPlayerUnit(); }
    public void setPlayerUnit(Unit unit) { unitManager.setPlayerUnit(unit); }
    public void spawnUnitNearCity(City city, String unitType) { unitManager.spawnUnitNearCity(city, unitType); }
    public void spawnSettlerNearCity(City city) { unitManager.spawnSettlerNearCity(city); }
    public void spawnScoutNearCity(City city) { unitManager.spawnScoutNearCity(city); }
    public void moveUnit(Unit unit, Hex targetHex) {
        unitManager.moveUnit(unit, targetHex);
        clearPath();
        if (onUnitSelected != null) onUnitSelected.run();
        if (onStatusChanged != null) onStatusChanged.run();
    }
    public void restUnit(Unit unit) { unitManager.restUnit(unit); }
    public void disbandUnit(Unit unit) { unitManager.disbandUnit(unit); }
    public void updateUnitsEndTurn() { unitManager.updateUnitsEndTurn(); }

    // CameraManager
    public double getCurrentHexSize() { return cameraManager.getCurrentHexSize(); }
    public double getMinHexSize() { return 12; }
    public double getMaxHexSize() { return 55; }
    public void setCanvasSize(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
        cameraManager.setCanvasSize(width, height);
        if (tileRenderer != null) {
            tileRenderer.setCanvasSize(width, height);
        }
    }
    public void moveCamera(double dx, double dy) { cameraManager.moveCamera(dx, dy); }
    public void zoomCamera(double factor, double mouseX, double mouseY) { cameraManager.zoomCamera(factor, mouseX, mouseY); }
    public void updateWorldBounds() { cameraManager.updateWorldBounds(); }
    public void updateWorldBoundsFromCanvas(double width, double height) { cameraManager.updateWorldBoundsFromCanvas(width, height); }
    public void recalculateHexSize(double width, double height) { cameraManager.recalculateHexSize(width, height); }
    public void centerMap() { cameraManager.centerMap(); }
    public void centerOnUnit(Unit unit) { cameraManager.centerOnUnit(unit); }
    public void centerOnHex(Hex hex) { cameraManager.centerOnHex(hex); }

    // CityViewManager
    public boolean isCityView() { return cityViewManager.isCityView(); }
    public City getZoomedCity() { return cityViewManager.getZoomedCity(); }
    public void enterCityView(City city) {
        cityViewManager.enterCityView(city);
        clearPath();
    }
    public void exitCityView() {
        cityViewManager.exitCityView();
        clearPath();
    }
    public boolean isWithinCityRadius(Hex hex) { return cityViewManager.isWithinCityRadius(hex); }

    // CityManager
    public List<City> getCities() { return cityManager.getCities(); }
    public void foundCity(Unit unit, String cityName) { cityManager.foundCity(unit, cityName); }
    public City findCityAtHex(Hex hex) { return cityManager.findCityAtHex(hex); }
    public void addProjectToCity(City city, String project) { cityManager.addProjectToCity(city, project); }

    // AssignmentManager
    public boolean isAssignmentMode() { return assignmentManager.isAssignmentMode(); }
    public City getAssignmentCity() { return assignmentManager.getAssignmentCity(); }
    public void toggleAssignmentMode() { assignmentManager.toggleAssignmentMode(); }
    public void handleAssignmentClick(Hex hex) { assignmentManager.handleAssignmentClick(hex); }

    // ========================================================================
    // Сеттеры и колбэки
    // ========================================================================

    public void setAdvisor(Advisor advisor) { this.advisor = advisor; }
    public void setUIManager(UIManager uiManager) { this.uiManager = uiManager; }
    public void setOnUnitSelected(Runnable r) { this.onUnitSelected = r; }
    public void setOnCitySelected(Runnable r) { this.onCitySelected = r; }
    public void setOnResourcesUpdated(Runnable r) { this.onResourcesUpdated = r; }
    public void setOnProgressUpdated(Runnable r) { this.onProgressUpdated = r; }
    public void setOnStatusChanged(Runnable r) { this.onStatusChanged = r; }

    // ========================================================================
    // Поиск объектов
    // ========================================================================

    public Tile findTileAtHex(Hex hex) {
        for (GameObject obj : world.getAllObjects()) {
            if (obj instanceof Tile) {
                Tile tile = (Tile) obj;
                if (tile.getHex().equals(hex)) return tile;
            }
        }
        return null;
    }

    public Tile findGlobalTileAtHex(Hex hex) { return findTileAtHex(hex); }

    public List<Unit> getUnitsAtHex(Hex hex) {
        if (isCityView()) return new ArrayList<>();
        List<Unit> result = new ArrayList<>();
        for (Unit unit : unitManager.getAllUnits()) {
            if (unit.getCurrentHex() != null && unit.getCurrentHex().equals(hex)) {
                result.add(unit);
            }
        }
        return result;
    }

    // ========================================================================
    // Информация о клетке
    // ========================================================================

    public static class TileInfo {
        public String coord, terrain, water, owner;
        public int food, production, gold, faith, culture;
        public String resources, improvements;
    }

    public TileInfo getTileInfo(Hex hex) {
        Tile tile = findTileAtHex(hex);
        if (tile == null) return null;
        TileInfo info = new TileInfo();
        TerrainType terrain = tile.getTerrain();
        info.coord = "(" + hex.col + ", " + hex.row + ")";
        info.terrain = terrain.getName();
        info.water = switch (terrain) {
            case RIVER -> "Пресная вода (река)";
            case OCEAN -> "Есть вода (море)";
            default -> "Нет воды";
        };
        City ownerCity = findCityAtHex(hex);
        info.owner = (ownerCity != null) ? ownerCity.getName() : "Ничья";
        info.food = terrain.getFood();
        info.production = terrain.getProduction();
        info.gold = 0;
        info.faith = 0;
        info.culture = 0;
        info.resources = "Нет";

        // Улучшения
        Improvement imp = tile.getImprovement();
        if (imp != null) {
            if (imp.isUnderConstruction()) {
                info.improvements = imp.getType().name() + " (строится)";
            } else {
                info.improvements = imp.getType().name();
            }
        } else {
            info.improvements = "Нет";
        }
        // Район
        District district = tile.getDistrict();
        if (district != null) {
            if (info.improvements.equals("Нет")) {
                info.improvements = "Район: " + district.getName();
            } else {
                info.improvements += " + Район: " + district.getName();
            }
        }
        // Центр города
        if (tile.isCityCenter()) {
            if (info.improvements.equals("Нет")) {
                info.improvements = "Центр города";
            } else {
                info.improvements += " (центр города)";
            }
        }

        return info;
    }

    // ========================================================================
    // Легитимность – факторы
    // ========================================================================

    public List<String> getLegitimacyFactors() {
        List<String> factors = new ArrayList<>();
        int leg = gameState.getLegitimacy();
        factors.add("Базовая легитимность: 50");

        int cityBonus = getCities().size() * 5;
        if (cityBonus > 0) factors.add("+ " + cityBonus + " за города (" + getCities().size() + ")");

        int totalPop = 0;
        for (City city : getCities()) totalPop += city.getPopulation();
        int popBonus = totalPop / 1000;
        if (popBonus > 0) factors.add("+ " + popBonus + " за население (" + totalPop + ")");

        int techCount = 0;
        for (TechNode node : techTree.getTechs()) {
            if (node.isResearched()) techCount++;
        }
        int techBonus = techCount * 2;
        if (techBonus > 0) factors.add("+ " + techBonus + " за изученные технологии (" + techCount + ")");

        int cultureBonus = getCulturePerTurn() / 2;
        if (cultureBonus > 0) factors.add("+ " + cultureBonus + " за культуру");

        int lawBonus = governmentManager.getTotalLegitimacyBonus();
        if (lawBonus != 0) factors.add((lawBonus > 0 ? "+ " : "") + lawBonus + " за законы");

        if (leg < 50) factors.add("⚠️ Низкая легитимность: бонусы снижены");
        if (leg > 80) factors.add("⭐ Высокая легитимность: бонусы увеличены");

        return factors;
    }

    // ========================================================================
    // Старт новой игры
    // ========================================================================

    public void setWaypointForSelectedUnit(Hex targetHex) {
        pendingTargetHex = targetHex;
        updatePathForHover(targetHex);
        confirmWaypoint();
    }

    public void startNewGame() {
        world = new World();
        hexGrid = new HexGrid(28);
        hexGrid.setPadding(200);

        fogOfWar.reset();
        tileRenderer.setHexGrid(hexGrid);
        tileRenderer.setCanvasSize(canvasWidth, canvasHeight);
        world.setTileRenderer(tileRenderer);

        unitManager.setWorldAndHexGrid(world, hexGrid);
        cityManager.setWorld(world);

        WorldGenerator generator = new WorldGenerator(world, hexGrid, cols, rows);
        Hex startHex = generator.generate();

        world.updateTileGeometries(hexGrid);

        Settler playerUnit = new Settler(startHex, hexGrid, techTree);
        world.addObject(playerUnit);
        unitManager.getAllUnits().add(playerUnit);
        unitManager.setPlayerUnit(playerUnit);

        cameraManager.setCurrentHexSize(28);
        centerMap();
        centerOnUnit(playerUnit);
        updateWorldBounds();

        cityManager.reset();
        selectionManager.clearSelection();
        placementManager.reset();
        turnManager.reset();
        fogManager.reset();
        cityViewManager.reset();
        assignmentManager.reset();
        governmentManager.reset();

        recalculateFog();

        if (advisor != null) advisor.reset();

        if (onStatusChanged != null) onStatusChanged.run();
        updateUI();

        historyTracker.clear();

        recalcIncome();
        recalculateLegitimacy();
        updateUI();

        clearPath();
    }

    // ========================================================================
    // Визуализация пути
    // ========================================================================

    public void clearPath() {
        currentPath.clear();
        currentStopPoints.clear();
        isWaypointPending = false;
        pendingTargetHex = null;
    }

    public void updatePathForHover(Hex hex) {
        if (hex == null || isCityView() || isAssignmentMode() || isPlacementMode() || isDistrictPlacementMode()) {
            clearPath();
            return;
        }

        Unit selected = getSelectedUnit();
        if (selected == null || !selected.canMove()) {
            clearPath();
            return;
        }

        List<Hex> fullPath = selected.findFullPath(hex, world, hexGrid);
        if (fullPath == null || fullPath.isEmpty()) {
            clearPath();
            return;
        }

        currentPath = fullPath;
        currentStopPoints = selected.calculateStopPoints(fullPath, world);
        isWaypointPending = true;
        pendingTargetHex = hex;
    }

    public void confirmWaypoint() {
        if (!isWaypointPending || pendingTargetHex == null) {
            updateStatus("Нет маршрута для подтверждения.");
            return;
        }

        Unit selected = getSelectedUnit();
        if (selected == null) {
            updateStatus("Юнит не выбран.");
            clearPath();
            return;
        }

        List<Hex> fullPath = new ArrayList<>(currentPath);
        if (fullPath.isEmpty()) {
            updateStatus("Путь пуст.");
            clearPath();
            return;
        }

        selected.setWaypoints(fullPath, world);
        updateStatus("Маршрут подтверждён. Остановок: " + selected.getStopPoints().size());

        boolean completed = selected.moveAlongWaypoint(world, hexGrid);
        if (completed) {
            updateStatus("Маршрут полностью пройден за один ход.");
        } else {
            int remaining = selected.getWaypoints().size() - 1;
            updateStatus("Пройден первый отрезок маршрута. Осталось клеток: " + remaining);
        }

        clearPath();
        recalculateFog();

        if (onUnitSelected != null) onUnitSelected.run();
        updateUI();
    }

    public void cancelWaypointForSelectedUnit() {
        Unit selected = getSelectedUnit();
        if (selected == null) return;
        selected.clearWaypoints();
        clearPath();
        updateStatus("Маршрут отменён.");
        if (onUnitSelected != null) onUnitSelected.run();
        updateUI();
    }

    public void executeWaypointsMovement() {
        for (Unit unit : unitManager.getAllUnits()) {
            if (unit.isWaypointMode() && unit.canMove()) {
                boolean completed = unit.moveAlongWaypoint(world, hexGrid);
                if (completed) {
                    if (unit == getSelectedUnit()) {
                        if (onUnitSelected != null) onUnitSelected.run();
                    }
                }
            }
        }
        recalculateFog();
        updateUI();
    }

    // ========================================================================
    // Завершение хода
    // ========================================================================

    public void endTurn() {
        turnManager.endTurn();
        clearPath();
        if (onStatusChanged != null) onStatusChanged.run();
        updateUI();
    }

    // ========================================================================
    // UI-методы
    // ========================================================================

    public void updateUI() {
        if (onResourcesUpdated != null) onResourcesUpdated.run();
        if (onProgressUpdated != null) onProgressUpdated.run();
        if (onStatusChanged != null) onStatusChanged.run();
        if (selectionManager.getSelectedUnit() != null && onUnitSelected != null) onUnitSelected.run();
        if (selectionManager.getSelectedCity() != null && onCitySelected != null) onCitySelected.run();
        if (uiManager != null) uiManager.updateResearchPanel();
    }

    public void updateStatus(String msg) {
        if (onStatusChanged != null) onStatusChanged.run();
    }

    // ========================================================================
    // Вспомогательные методы
    // ========================================================================

    void recalcIncome() {
        turnManager.recalcIncome();
    }

    public HistoryTracker getHistoryTracker() { return historyTracker; }

    void recalculateLegitimacy() {
        turnManager.recalculateLegitimacy();
    }
}
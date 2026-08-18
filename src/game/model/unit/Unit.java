package game.model.unit;

import engine.core.GameObject;
import game.model.city.City;
import game.model.research.TechTree;
import game.model.world.Hex;
import game.model.world.HexGrid;
import game.model.world.TerrainType;
import game.model.world.Tile;
import game.model.world.World;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public abstract class Unit extends GameObject {
    protected Color color;
    protected int health;
    protected int attack;
    protected int movementPoints;
    protected int maxMovementPoints;
    protected int actionPoints;
    protected int maxActionPoints;
    protected String owner;
    protected boolean canFoundCity;
    protected Hex currentHex;
    protected double width, height;
    protected HexGrid hexGrid;
    protected boolean hasMovedThisTurn = false;
    protected int sightRadius = 3;
    protected City homeCity;
    protected TechTree techTree;

    protected int population = 0;
    protected int squadMembers = 0;
    protected int fatigue = 0;
    protected boolean isResting = false;

    private static final int FATIGUE_THRESHOLD = 80;
    private static final int LOSS_PER_TICK = 2;

    // ========================================================================
    // СИСТЕМА МАРШРУТОВ
    // ========================================================================
    private List<Hex> waypoints = new ArrayList<>();
    private List<Hex> stopPoints = new ArrayList<>();
    private boolean isWaypointMode = false;

    // ========================================================================
    // КОНСТРУКТОР
    // ========================================================================
    public Unit(Hex startHex, double width, double height, Color color, int health, int attack,
                int maxMovementPoints, int maxActionPoints, String owner, boolean canFoundCity,
                HexGrid hexGrid, TechTree techTree) {
        super(0, 0, width, height);
        this.currentHex = startHex;
        this.width = width;
        this.height = height;
        this.color = color;
        this.health = health;
        this.attack = attack;
        this.maxMovementPoints = maxMovementPoints;
        this.movementPoints = maxMovementPoints;
        this.maxActionPoints = maxActionPoints;
        this.actionPoints = maxActionPoints;
        this.owner = owner;
        this.canFoundCity = canFoundCity;
        this.hexGrid = hexGrid;
        this.techTree = techTree;
        setZIndex(1);
        this.hasMovedThisTurn = false;
        this.sightRadius = 3;
        this.homeCity = null;
        this.fatigue = 0;
        this.isResting = false;
    }

    // --- Геттеры/сеттеры ---
    public City getHomeCity() { return homeCity; }
    public void setHomeCity(City city) { this.homeCity = city; }
    public Hex getCurrentHex() { return currentHex; }
    public void setCurrentHex(Hex hex) { this.currentHex = hex; }
    public int getMovementPoints() { return movementPoints; }
    public int getMaxMovementPoints() { return maxMovementPoints; }
    public int getActionPoints() { return actionPoints; }
    public int getMaxActionPoints() { return maxActionPoints; }
    public String getOwner() { return owner; }
    public boolean canFoundCity() { return canFoundCity; }
    public boolean canMove() { return movementPoints > 0; }
    public boolean canAct() { return actionPoints > 0; }

    public int getPopulation() { return population; }
    public void setPopulation(int population) { this.population = Math.max(0, population); }
    public int getSquadMembers() { return squadMembers; }
    public void setSquadMembers(int squadMembers) { this.squadMembers = Math.max(0, squadMembers); }
    public int getFatigue() { return fatigue; }
    public void setFatigue(int fatigue) { this.fatigue = Math.max(0, Math.min(100, fatigue)); }
    public boolean isResting() { return isResting; }
    public void setResting(boolean resting) { this.isResting = resting; }
    public boolean hasMovedThisTurn() { return hasMovedThisTurn; }
    public void setHasMovedThisTurn(boolean moved) { this.hasMovedThisTurn = moved; }
    public int getSightRadius() { return sightRadius; }
    public void setSightRadius(int sightRadius) { this.sightRadius = sightRadius; }

    // --- Геттеры для маршрутов ---
    public List<Hex> getWaypoints() { return waypoints; }
    public List<Hex> getStopPoints() { return stopPoints; }
    public boolean isWaypointMode() { return isWaypointMode; }

    public void setWaypoints(List<Hex> fullPath, World world) {
        if (fullPath == null || fullPath.isEmpty()) {
            clearWaypoints();
            return;
        }
        this.waypoints = new ArrayList<>(fullPath);
        this.stopPoints = calculateStopPoints(fullPath, world);
        this.isWaypointMode = true;
    }

    public void clearWaypoints() {
        waypoints.clear();
        stopPoints.clear();
        isWaypointMode = false;
    }

    // ========================================================================
    // УСТАЛОСТЬ И ПОТЕРИ
    // ========================================================================

    public void checkFatigueAndCasualties() {
        if (fatigue > FATIGUE_THRESHOLD) {
            if (population > 0) {
                population = Math.max(0, population - LOSS_PER_TICK);
            } else if (squadMembers > 0) {
                squadMembers = Math.max(0, squadMembers - LOSS_PER_TICK);
            }
            if (homeCity != null) {
                homeCity.setHappiness(homeCity.getHappiness() - 1);
                if (homeCity.getHappiness() < 0) homeCity.setHappiness(0);
            }
        }
        if (isResting) {
            fatigue = Math.max(0, fatigue - 3);
        } else {
            fatigue = Math.min(100, fatigue + 1);
        }
    }

    protected void addFatigue(int amount) {
        setFatigue(this.fatigue + amount);
    }

    public static String getRequiredTech(String unitType) {
        switch (unitType) {
            case "warrior": return "Обработка кремня";
            case "archer": return "Лук и стрелы";   // обновим, если в дереве другое название
            case "chariot": return "Колесо (раннее)";
            case "bronze_swordsman": return "Бронзовый сплав";
            case "horseman": return "Одомашнивание лошади";
            case "galley": return "Мореходство";
            case "battering_ram": return "Осадное дело";
            case "scout": return "Приручение собаки";   // <-- НОВОЕ
            default: return null;
        }
    }

    // ========================================================================
    // ОТРИСОВКА
    // ========================================================================

    @Override
    public void render(GraphicsContext gc) {
        if (currentHex == null || hexGrid == null) return;
        double[] center = hexGrid.hexToScreen(currentHex.col, currentHex.row);
        double x = center[0] - width/2;
        double y = center[1] - height/2;
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);
        gc.setFill(Color.WHITE);
        gc.fillText("MP:" + movementPoints, x + 2, y + 12);
        gc.fillText("AP:" + actionPoints, x + 2, y + 25);
        if (population > 0) {
            gc.fillText("👤" + population, x + 2, y + 38);
        } else if (squadMembers > 0) {
            gc.fillText("👥" + squadMembers, x + 2, y + 38);
        }
        if (homeCity != null) {
            gc.fillText("🏠" + homeCity.getName(), x + 2, y + 51);
        }
        gc.fillText("😩" + fatigue + "%", x + 2, y + 64);
        gc.fillText("⛺" + (isResting ? "Да" : "Нет"), x + 2, y + 77);
        if (isWaypointMode && !waypoints.isEmpty()) {
            gc.setFill(Color.rgb(255, 215, 0, 0.8));
            gc.fillText("📌 Маршрут: " + (waypoints.size() - 1) + " кл.", x + 2, y + 90);
        }
    }

    public abstract void update(double deltaTime);

    // ========================================================================
    // ПОИСК ДОСТИЖИМЫХ КЛЕТОК (с ограничением по очкам)
    // ========================================================================

    public Map<Hex, Integer> getReachableHexes(World world, HexGrid grid) {
        Map<Hex, Integer> reachable = new HashMap<>();
        if (currentHex == null || movementPoints <= 0) return reachable;

        PriorityQueue<Hex> queue = new PriorityQueue<>(Comparator.comparingInt(reachable::get));
        queue.add(currentHex);
        reachable.put(currentHex, 0);

        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            int currentCost = reachable.get(current);

            for (Hex neighbor : current.neighbors()) {
                Tile tile = findTile(world, neighbor);
                if (tile == null) continue;
                TerrainType terrain = tile.getTerrain();
                if (!terrain.isPassable()) continue;

                int cost = terrain.getMovementCost();
                if (terrain.isRiver()) {
                    cost = 100; // непроходимо
                }

                int newCost = currentCost + cost;
                if (newCost <= movementPoints && newCost < reachable.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    reachable.put(neighbor, newCost);
                    queue.add(neighbor);
                }
            }
        }

        reachable.remove(currentHex);
        return reachable;
    }

    // ========================================================================
    // МГНОВЕННОЕ ПЕРЕМЕЩЕНИЕ (один шаг)
    // ========================================================================

    public boolean moveTo(Hex targetHex, World world, HexGrid grid) {
        Map<Hex, Integer> reachable = getReachableHexes(world, grid);
        if (!reachable.containsKey(targetHex)) return false;

        int cost = reachable.get(targetHex);
        if (cost > movementPoints) return false;

        boolean crossedRiver = false;
        List<Hex> path = reconstructPath(world, grid, targetHex);
        if (path != null) {
            for (int i = 0; i < path.size() - 1; i++) {
                Hex from = path.get(i);
                Hex to = path.get(i + 1);
                Tile tileTo = findTile(world, to);
                if (tileTo != null && tileTo.getTerrain().isRiver()) {
                    crossedRiver = true;
                    break;
                }
            }
        }

        int desertSteps = countDesertSteps(path, world);
        if (desertSteps > 0) {
            addFatigue(desertSteps * 2);
        }

        this.currentHex = targetHex;
        movementPoints -= cost;

        if (crossedRiver) {
            movementPoints = 0;
            applyRiverCrossingPenalty(world);
        }

        this.hasMovedThisTurn = true;
        return true;
    }

    protected void applyRiverCrossingPenalty(World world) {
        // переопределяется в наследниках (Settler, Scout)
    }

    // ========================================================================
    // ВОССТАНОВЛЕНИЕ ПУТИ (для визуализации)
    // ========================================================================

    public List<Hex> reconstructPathTo(Hex target, World world, HexGrid grid) {
        List<Hex> path = reconstructPath(world, grid, target);
        return path != null ? path : new ArrayList<>();
    }

    // ========================================================================
    // ПОСТРОЕНИЕ ПОЛНОГО ПУТИ (без ограничения)
    // ========================================================================

    public List<Hex> findFullPath(Hex target, World world, HexGrid grid) {
        if (currentHex == null || target == null) return null;
        if (currentHex.equals(target)) {
            List<Hex> path = new ArrayList<>();
            path.add(currentHex);
            return path;
        }

        Map<Hex, Hex> parent = new HashMap<>();
        Map<Hex, Integer> costSoFar = new HashMap<>();
        PriorityQueue<Hex> queue = new PriorityQueue<>(Comparator.comparingInt(costSoFar::get));
        queue.add(currentHex);
        costSoFar.put(currentHex, 0);
        parent.put(currentHex, null);

        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            if (current.equals(target)) {
                List<Hex> path = new ArrayList<>();
                Hex step = target;
                while (step != null) {
                    path.add(step);
                    step = parent.get(step);
                }
                Collections.reverse(path);
                return path;
            }
            int currentCost = costSoFar.get(current);
            if (currentCost > 5000) continue;

            for (Hex neighbor : current.neighbors()) {
                Tile tile = findTile(world, neighbor);
                if (tile == null) continue;
                TerrainType terrain = tile.getTerrain();
                if (!terrain.isPassable()) continue;
                int moveCost = terrain.getMovementCost();
                if (terrain.isRiver()) moveCost = 100;
                int newCost = currentCost + moveCost;
                if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                    costSoFar.put(neighbor, newCost);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }

    // ========================================================================
    // ВЫЧИСЛЕНИЕ ТОЧЕК ОСТАНОВКИ
    // ========================================================================

    public List<Hex> calculateStopPoints(List<Hex> fullPath, World world) {
        List<Hex> stops = new ArrayList<>();
        if (fullPath == null || fullPath.size() < 2) {
            if (fullPath != null && !fullPath.isEmpty()) stops.add(fullPath.get(0));
            return stops;
        }

        int remainingMP = maxMovementPoints;
        int accumulated = 0;
        for (int i = 1; i < fullPath.size(); i++) {
            Hex to = fullPath.get(i);
            Tile tile = findTile(world, to);
            if (tile == null) break;
            int moveCost = tile.getTerrain().getMovementCost();
            if (tile.getTerrain().isRiver()) moveCost = 100;
            accumulated += moveCost;
            if (accumulated > remainingMP) {
                Hex stop = fullPath.get(i - 1);
                stops.add(stop);
                accumulated = 0;
                i--; // повторно рассмотрим переход от stop к to
            }
        }
        if (accumulated <= remainingMP && !fullPath.isEmpty()) {
            stops.add(fullPath.get(fullPath.size() - 1));
        }
        // Убираем дубликаты
        List<Hex> uniqueStops = new ArrayList<>();
        for (Hex h : stops) {
            if (uniqueStops.isEmpty() || !uniqueStops.get(uniqueStops.size() - 1).equals(h)) {
                uniqueStops.add(h);
            }
        }
        return uniqueStops;
    }

    // ========================================================================
    // ДВИЖЕНИЕ ПО МАРШРУТУ (ИСПРАВЛЕНО: тратит все очки)
    // ========================================================================

    public boolean moveAlongWaypoint(World world, HexGrid grid) {
        if (!isWaypointMode || waypoints.isEmpty()) {
            clearWaypoints();
            return true;
        }

        // Удаляем текущую позицию из начала, если она там есть
        if (waypoints.get(0).equals(currentHex)) {
            waypoints.remove(0);
            if (waypoints.isEmpty()) {
                clearWaypoints();
                return true;
            }
        }

        // Определяем первую точку остановки, которая ещё не достигнута
        Hex nextStop = null;
        for (Hex stop : stopPoints) {
            if (!stop.equals(currentHex)) {
                nextStop = stop;
                break;
            }
        }
        if (nextStop == null) {
            clearWaypoints();
            return true;
        }

        // Двигаемся, пока есть очки движения и есть клетки в пути
        while (movementPoints > 0 && !waypoints.isEmpty()) {
            Hex next = waypoints.get(0);
            Map<Hex, Integer> reachable = getReachableHexes(world, grid);
            if (!reachable.containsKey(next)) {
                break;
            }
            int cost = reachable.get(next);
            if (cost > movementPoints) {
                break; // не хватает очков для следующего шага
            }
            if (moveTo(next, world, grid)) {
                waypoints.remove(0);
                // После moveTo movementPoints уже уменьшен
                if (currentHex.equals(nextStop)) {
                    break; // достигли остановки
                }
            } else {
                break;
            }
        }

        // Если достигли остановки или не осталось очков, пересчитываем stopPoints для оставшегося пути
        if (!waypoints.isEmpty()) {
            List<Hex> remainingPath = new ArrayList<>();
            remainingPath.add(currentHex);
            remainingPath.addAll(waypoints);
            this.stopPoints = calculateStopPoints(remainingPath, world);
        } else {
            clearWaypoints();
            return true;
        }

        return false;
    }

    // ========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ========================================================================

    private List<Hex> reconstructPath(World world, HexGrid grid, Hex target) {
        Map<Hex, Hex> parent = new HashMap<>();
        Queue<Hex> queue = new LinkedList<>();
        queue.add(currentHex);
        parent.put(currentHex, null);

        while (!queue.isEmpty()) {
            Hex cur = queue.poll();
            if (cur.equals(target)) {
                List<Hex> path = new ArrayList<>();
                Hex step = target;
                while (step != null) {
                    path.add(step);
                    step = parent.get(step);
                }
                Collections.reverse(path);
                return path;
            }
            for (Hex neighbor : cur.neighbors()) {
                if (parent.containsKey(neighbor)) continue;
                Tile tile = findTile(world, neighbor);
                if (tile == null || !tile.getTerrain().isPassable()) continue;
                parent.put(neighbor, cur);
                queue.add(neighbor);
            }
        }
        return null;
    }

    private int countDesertSteps(List<Hex> path, World world) {
        if (path == null) return 0;
        int count = 0;
        for (Hex hex : path) {
            Tile tile = findTile(world, hex);
            if (tile != null && tile.getTerrain() == TerrainType.DESERT) {
                count++;
            } else {
                count = 0;
            }
        }
        return count;
    }

    protected Tile findTile(World world, Hex hex) {
        for (GameObject obj : world.getAllObjects()) {
            if (obj instanceof Tile) {
                Tile tile = (Tile) obj;
                if (tile.getHex().equals(hex)) return tile;
            }
        }
        return null;
    }

    public void foundCity() {
        if (!canFoundCity) return;
        if (actionPoints <= 0) return;
        actionPoints--;
    }

    public void disband() {}
    public void resetMovementPoints() { movementPoints = maxMovementPoints; }
    public void resetActionPoints() { actionPoints = maxActionPoints; }
}
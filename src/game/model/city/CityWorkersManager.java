package game.model.city;

import game.model.world.Hex;
import game.model.world.Improvement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Управление назначением горожан на клетки территории.
 * Хранит карту assignedCitizens и предоставляет методы для назначения/снятия,
 * расчёта свободных рабочих и т.д.
 */
public class CityWorkersManager {

    private final City city;

    // Клетка → количество назначенных горожан (в единицах по 50 чел.)
    private Map<Hex, Integer> assignedCitizens = new HashMap<>();
    private static final int CITIZENS_PER_TILE = 50;

    // Количество зарезервированных рабочих для строек
    private int reservedWorkers = 0;

    public CityWorkersManager(City city) {
        this.city = city;
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public Map<Hex, Integer> getAssignedCitizens() {
        return assignedCitizens;
    }

    public int getReservedWorkers() {
        return reservedWorkers;
    }

    public void setReservedWorkers(int reserved) {
        this.reservedWorkers = Math.max(0, reserved);
    }

    public void addReservedWorkers(int amount) {
        this.reservedWorkers += Math.max(0, amount);
    }

    public void subtractReservedWorkers(int amount) {
        this.reservedWorkers = Math.max(0, this.reservedWorkers - Math.max(0, amount));
    }

    // ========================================================================
    // Назначение и снятие горожан
    // ========================================================================

    public boolean assignCitizen(Hex hex) {
        Set<Hex> tiles = city.getTiles();
        if (!tiles.contains(hex)) return false;
        int current = assignedCitizens.getOrDefault(hex, 0);
        int required = getRequiredWorkers(hex);
        if (current >= required) return false;

        int toAdd = required - current;
        int totalWorkers = city.getPopulation() / CITIZENS_PER_TILE;
        int used = getTotalAssigned() + reservedWorkers;
        int available = totalWorkers - used;
        if (available <= 0) return false;

        if (toAdd > available) toAdd = available;
        assignedCitizens.put(hex, current + toAdd);
        return true;
    }

    public boolean unassignCitizen(Hex hex) {
        if (hex.equals(city.getCenter())) return false;
        if (!assignedCitizens.containsKey(hex)) return false;
        assignedCitizens.remove(hex);
        return true;
    }

    // ========================================================================
    // Получение данных о назначениях
    // ========================================================================

    public int getAssignedCount(Hex hex) {
        return assignedCitizens.getOrDefault(hex, 0);
    }

    public int getTotalAssigned() {
        int total = 0;
        for (int v : assignedCitizens.values()) total += v;
        return total;
    }

    public int getRequiredWorkers(Hex hex) {
        Improvement imp = city.getImprovementAt(hex);
        if (imp != null) return imp.getWorkersToOperate();
        return 1;
    }

    public boolean isFullyAssigned(Hex hex) {
        return assignedCitizens.getOrDefault(hex, 0) >= getRequiredWorkers(hex);
    }

    public Set<Hex> getAssignedTiles() {
        return assignedCitizens.keySet();
    }

    public boolean isAssigned(Hex hex) {
        return assignedCitizens.containsKey(hex);
    }

    // ========================================================================
    // Расчёт свободных рабочих
    // ========================================================================

    public int getMaxCitizens() {
        return city.getPopulation() / CITIZENS_PER_TILE;
    }

    public int getUsedCitizens() {
        return getTotalAssigned();
    }

    public int getCitizensPerTile() {
        return CITIZENS_PER_TILE;
    }

    public int getFreeWorkers() {
        int total = city.getPopulation() / CITIZENS_PER_TILE;
        int used = getTotalAssigned() + reservedWorkers;
        return total - used;
    }

    // ========================================================================
    // Проверка доступности рабочей силы для строек
    // ========================================================================

    public boolean hasEnoughWorkers(int needed) {
        return getFreeWorkers() >= needed;
    }

    // ========================================================================
    // Сброс для новой игры (кроме reservedWorkers, он сбрасывается отдельно)
    // ========================================================================

    public void reset() {
        assignedCitizens.clear();
        reservedWorkers = 0;
    }

    // ========================================================================
    // Инициализация после создания города (назначение в центр)
    // ========================================================================

    public void initCenter(Hex center) {
        assignedCitizens.put(center, 1);
    }
}
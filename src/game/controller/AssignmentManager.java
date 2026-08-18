package game.controller;

import game.model.city.City;
import game.model.world.Hex;

/**
 * Управление режимом назначения горожан.
 * Позволяет входить в режим, выходить из него, обрабатывать клики по клеткам.
 */
public class AssignmentManager {

    private final GameController controller;

    private boolean assignmentMode = false;
    private City assignmentCity = null;

    public AssignmentManager(GameController controller) {
        this.controller = controller;
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public boolean isAssignmentMode() {
        return assignmentMode;
    }

    public City getAssignmentCity() {
        return assignmentCity;
    }

    // ========================================================================
    // Включение/выключение режима
    // ========================================================================

    public void toggleAssignmentMode() {
        if (!controller.isCityView()) return;
        assignmentMode = !assignmentMode;
        assignmentCity = assignmentMode ? controller.getZoomedCity() : null;
        if (controller.onStatusChanged != null) controller.onStatusChanged.run();
        controller.updateUI();
    }

    // ========================================================================
    // Обработка клика по клетке в режиме назначения
    // ========================================================================

    public void handleAssignmentClick(Hex hex) {
        if (!assignmentMode || assignmentCity == null) return;
        if (!controller.isWithinCityRadius(hex)) {
            controller.updateStatus("Клетка вне зоны города.");
            return;
        }
        if (assignmentCity.isAssigned(hex)) {
            if (assignmentCity.unassignCitizen(hex)) {
                controller.updateUI();
                if (controller.onStatusChanged != null) controller.onStatusChanged.run();
            }
        } else {
            if (assignmentCity.assignCitizen(hex)) {
                controller.updateUI();
                if (controller.onStatusChanged != null) controller.onStatusChanged.run();
            } else {
                controller.updateStatus("Недостаточно жителей или клетка недоступна.");
            }
        }
    }

    // ========================================================================
    // Принудительный выход из режима (например, при выходе из городского вида)
    // ========================================================================

    public void exitAssignmentMode() {
        if (assignmentMode) {
            assignmentMode = false;
            assignmentCity = null;
            if (controller.onStatusChanged != null) controller.onStatusChanged.run();
            controller.updateUI();
        }
    }

    // ========================================================================
    // Сброс (новая игра)
    // ========================================================================

    public void reset() {
        assignmentMode = false;
        assignmentCity = null;
    }
}
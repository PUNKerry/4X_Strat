package game.controller;

import game.UI.UIManager;
import game.model.city.City;
import game.model.unit.Unit;
import game.model.world.Hex;
import game.model.world.Tile;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.util.List;

public class InputHandler {
    private GameController controller;
    private UIManager uiManager;
    private Canvas canvas;

    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;

    public InputHandler(GameController controller, UIManager uiManager, Canvas canvas) {
        this.controller = controller;
        this.uiManager = uiManager;
        this.canvas = canvas;
        attachEvents();
    }

    private void attachEvents() {
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseClicked(this::onMouseClicked);
        canvas.setOnMouseMoved(this::onMouseMoved);
        canvas.setOnMouseExited(e -> {
            uiManager.getInfoPanel().setVisible(false);
            if (controller.getSelectedUnit() != null) {
                controller.updatePathForHover(null);
            }
        });
        canvas.setOnScroll(this::onScroll);
    }

    public void onKeyPressed(KeyCode code) {
        if (code == KeyCode.W) controller.moveCamera(0, 20);
        else if (code == KeyCode.S) controller.moveCamera(0, -20);
        else if (code == KeyCode.A) controller.moveCamera(20, 0);
        else if (code == KeyCode.D) controller.moveCamera(-20, 0);
        else if (code == KeyCode.ESCAPE && controller.getSelectedUnit() != null) {
            controller.cancelWaypointForSelectedUnit();
            controller.clearHighlights();
            controller.selectUnit(null);
            uiManager.updateUnitPanel(null);
            uiManager.updateStatus("Действие отменено.");
        }
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            isDragging = true;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (isDragging) {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;
            controller.moveCamera(dx, dy);
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    private void onMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            isDragging = false;
        }
    }

    private void onMouseClicked(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        double x = e.getX(), y = e.getY();

        Hex clickedHex = controller.getCurrentHexGrid().screenToHex(x, y);
        Tile tile = controller.findTileAtHex(clickedHex);
        if (tile == null) return;

        if (controller.isCityView()) {
            if (!controller.isWithinCityRadius(clickedHex)) {
                return;
            }
        }

        // --- Режим назначения горожан ---
        if (controller.isAssignmentMode()) {
            controller.handleAssignmentClick(clickedHex);
            return;
        }

        // --- Режим размещения улучшений ---
        if (controller.isCityView() && controller.isPlacementMode()) {
            controller.handlePlacementClick(clickedHex);
            return;
        }

        // --- Режим размещения районов ---
        if (controller.isCityView() && controller.isDistrictPlacementMode()) {
            controller.handleDistrictPlacementClick(clickedHex);
            return;
        }

        // Клик по центру города
        if (tile.isCityCenter()) {
            if (controller.isCityView()) {
                uiManager.updateStatus("Вы уже в городском режиме.");
                return;
            }
            City city = controller.findCityAtHex(clickedHex);
            if (city != null) {
                if (controller.getSelectedUnit() != null) {
                    controller.clearHighlights();
                    controller.selectUnit(null);
                }
                controller.selectCity(city);
                uiManager.updateCityPanel(city);
                uiManager.updateStatus("Город выбран.");
                return;
            }
        }

        // ================================================================
        // УСТАНОВКА МАРШРУТА ПРИ КЛИКЕ ПО ЦЕЛЕВОЙ КЛЕТКЕ
        // ================================================================
        Unit selected = controller.getSelectedUnit();
        if (!controller.isCityView() && selected != null && selected.canMove()) {
            List<Hex> currentPath = controller.getCurrentPath();
            if (currentPath != null && !currentPath.isEmpty()) {
                Hex targetHex = currentPath.get(currentPath.size() - 1);
                if (targetHex.equals(clickedHex)) {
                    controller.setWaypointForSelectedUnit(clickedHex);
                    controller.clearHighlights();
                    uiManager.updateUnitPanel(selected);
                    return;
                }
            }
        }

        // --- Перемещение юнита (мгновенное, если не установлен маршрут) ---
        if (!controller.isCityView() && selected != null && controller.getReachableHexes() != null) {
            if (controller.getReachableHexes().containsKey(clickedHex)) {
                // Если маршрут не активен, перемещаем мгновенно
                if (!selected.isWaypointMode()) {
                    controller.moveUnit(selected, clickedHex);
                    uiManager.updateStatus("Юнит перемещён.");
                    if (!selected.canMove()) {
                        controller.selectUnit(null);
                        uiManager.updateUnitPanel(null);
                    } else {
                        controller.highlightAvailableMoves(selected);
                        uiManager.updateUnitPanel(selected);
                    }
                    controller.clearPath();
                    return;
                }
            }
        }

        // --- Выбор юнита ---
        if (!controller.isCityView()) {
            List<Unit> unitsHere = controller.getUnitsAtHex(clickedHex);
            if (!unitsHere.isEmpty()) {
                Unit current = controller.getSelectedUnit();
                if (current != null && current.getCurrentHex().equals(clickedHex)) {
                    int index = unitsHere.indexOf(current);
                    int newIndex = (index + 1) % unitsHere.size();
                    Unit newUnit = unitsHere.get(newIndex);
                    if (newUnit.canMove() || newUnit.canAct()) {
                        controller.selectUnit(newUnit);
                        uiManager.updateUnitPanel(newUnit);
                        uiManager.updateStatus("Юнит выбран (переключено).");
                        return;
                    } else {
                        uiManager.updateStatus("У следующего юнита нет очков действия.");
                        return;
                    }
                } else {
                    Unit unit = unitsHere.get(0);
                    if (unit.canMove() || unit.canAct()) {
                        controller.selectUnit(unit);
                        uiManager.updateUnitPanel(unit);
                        uiManager.updateStatus("Юнит выбран.");
                        return;
                    } else {
                        uiManager.updateStatus("Нет очков движения/действия.");
                        controller.clearHighlights();
                        controller.selectUnit(null);
                        uiManager.updateUnitPanel(null);
                        return;
                    }
                }
            }
        }

        // Снятие выделения
        if (!controller.isCityView()) {
            controller.clearHighlights();
            controller.selectUnit(null);
            controller.selectCity(null);
            uiManager.updateUnitPanel(null);
            uiManager.updateCityPanel(null);
            uiManager.updateStatus("Выберите юнита");
        }
    }

    private void onMouseMoved(MouseEvent e) {
        if (!canvas.isVisible()) return;
        double x = e.getX(), y = e.getY();
        Hex hex = controller.getCurrentHexGrid().screenToHex(x, y);
        Tile tile = controller.findTileAtHex(hex);
        if (tile == null) {
            uiManager.getInfoPanel().setVisible(false);
            controller.updatePathForHover(null);
            return;
        }
        if (controller.isCityView()) {
            if (!controller.isWithinCityRadius(hex)) {
                uiManager.getInfoPanel().setVisible(false);
                controller.updatePathForHover(null);
                return;
            }
        }
        uiManager.updateInfoPanel(hex, tile);
        uiManager.getInfoPanel().setVisible(true);

        // Обновляем путь при наведении
        if (controller.getSelectedUnit() != null && !controller.isCityView() &&
                !controller.isAssignmentMode() && !controller.isPlacementMode() &&
                !controller.isDistrictPlacementMode()) {
            controller.updatePathForHover(hex);
        } else {
            controller.updatePathForHover(null);
        }
    }

    private void onScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        double factor = (delta > 0) ? 1.1 : 0.9;
        controller.zoomCamera(factor, event.getX(), event.getY());
    }
}
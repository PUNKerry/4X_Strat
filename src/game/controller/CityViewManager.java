package game.controller;

import game.model.city.City;
import game.model.world.Hex;

/**
 * Управление городским видом: вход, выход, проверка радиуса,
 * сохранение/восстановление параметров камеры через CameraManager.
 */
public class CityViewManager {

    private final GameController controller;
    private final CameraManager cameraManager;

    private boolean isCityView = false;
    private City cityViewCenter = null;
    private static final int CITY_VIEW_RADIUS = 6;

    public CityViewManager(GameController controller, CameraManager cameraManager) {
        this.controller = controller;
        this.cameraManager = cameraManager;
    }

    // ========================================================================
    // Геттеры
    // ========================================================================

    public boolean isCityView() {
        return isCityView;
    }

    public City getZoomedCity() {
        return cityViewCenter;
    }

    // ========================================================================
    // Вход и выход из городского вида
    // ========================================================================

    public void enterCityView(City city) {
        if (city == null || isCityView) return;

        // Выход из режимов, если они активны
        if (controller.isAssignmentMode()) controller.toggleAssignmentMode();
        if (controller.isPlacementMode()) controller.exitPlacementMode();
        if (controller.isDistrictPlacementMode()) controller.exitDistrictPlacementMode();

        // Сохраняем состояние камеры
        cameraManager.saveCameraState();

        cityViewCenter = city;
        isCityView = true;

        // Устанавливаем размер гексов для городского вида
        double targetHexSize = cameraManager.calculateHexSizeForCityView(CITY_VIEW_RADIUS);
        cameraManager.setCurrentHexSize(targetHexSize);
        controller.getHexGrid().setHexSize(targetHexSize);

        // Центрируем на городе
        cameraManager.centerOnHex(city.getCenter());
        controller.getHexGrid().setBoundsEnabled(false);

        // Обновляем UI
        if (controller.onCitySelected != null) controller.onCitySelected.run();
        controller.updateUI();

        if (controller.getAdvisor() != null) {
            controller.getAdvisor().showCityViewTutorial();
        }
    }

    public void exitCityView() {
        if (!isCityView) return;

        // Выход из режимов
        if (controller.isAssignmentMode()) controller.toggleAssignmentMode();
        if (controller.isPlacementMode()) controller.exitPlacementMode();
        if (controller.isDistrictPlacementMode()) controller.exitDistrictPlacementMode();

        isCityView = false;
        cityViewCenter = null;

        // Восстанавливаем состояние камеры
        cameraManager.restoreCameraState();
        controller.getHexGrid().setBoundsEnabled(true);
        controller.updateWorldBounds();

        // Обновляем UI
        if (controller.onCitySelected != null) controller.onCitySelected.run();
        controller.updateUI();
    }

    // ========================================================================
    // Проверка радиуса
    // ========================================================================

    public boolean isWithinCityRadius(Hex hex) {
        if (!isCityView || cityViewCenter == null) return true;
        return cityViewCenter.getCenter().distanceTo(hex) <= CITY_VIEW_RADIUS;
    }

    // ========================================================================
    // Сброс (новая игра)
    // ========================================================================

    public void reset() {
        if (isCityView) {
            exitCityView();
        }
        isCityView = false;
        cityViewCenter = null;
    }
}
package game.controller;

import game.UI.UIManager;

public class Advisor {
    private UIManager uiManager;
    private boolean tutorialShownSettler = false;
    private boolean tutorialShownFirstCity = false;
    private boolean tutorialShownCityView = false;
    private boolean tutorialShownFatigue = false;
    private boolean tutorialShownHousing = false;
    private boolean tutorialShownExpansion = false;
    private boolean tutorialShownCenterImprovement = false;

    public Advisor(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    public void reset() {
        tutorialShownSettler = false;
        tutorialShownFirstCity = false;
        tutorialShownCityView = false;
        tutorialShownFatigue = false;
        tutorialShownHousing = false;
        tutorialShownExpansion = false;
        tutorialShownCenterImprovement = false;
    }

    // --- Начальное сообщение о поселенце и воде ---
    public void showSettlerAndWaterTutorial() {
        if (tutorialShownSettler || uiManager == null) return;
        tutorialShownSettler = true;
        String title = "👨‍🌾 Добро пожаловать в мир стратегии!";
        String message = "У вас есть ПОСЕЛЕНЕЦ – ключевой юнит для основания городов.\n" +
                "Он может передвигаться и основать город на выбранной клетке.\n" +
                "ВНИМАНИЕ: у поселенца есть население (изначально 500 чел.).\n" +
                "Каждый ход он теряет 1-2 чел. (больше при движении).\n" +
                "Если население упадёт до 0 – юнит исчезнет.\n" +
                "Отдых (кнопка 'Отдохнуть') восстанавливает силы и замедляет потерю.\n\n" +
                "💧 ВАЖНО: при выборе места для города учитывайте наличие пресной воды (реки) в радиусе 3 клеток.\n" +
                "Без пресной воды рост населения будет ограничен (еда уменьшена на 50%).\n" +
                "Используйте поселенца с умом, чтобы основать первый город!";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showFirstCityTutorial() {
        if (tutorialShownFirstCity || uiManager == null) return;
        tutorialShownFirstCity = true;
        String title = "🏛️ Первый город основан!";
        String message = "Теперь вы можете управлять городом, кликнув на его центр (золотой круг).\n" +
                "Откроется панель города справа, где можно строить юниты, улучшения и районы.\n" +
                "Не забывайте следить за едой и производством!\n\n" +
                "💧 Проверьте наличие пресной воды в радиусе 3 клеток – это влияет на рост населения.\n" +
                "Если воды нет, вы увидели предупреждение, а рост будет замедлен.";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showFreshWaterWarning(String cityName) {
        if (uiManager == null) return;
        String title = "💧 Недостаток пресной воды!";
        String message = "Город '" + cityName + "' основан в месте, где нет пресной воды в радиусе 3 клеток.\n" +
                "Это ограничивает рост населения (прирост еды уменьшен на 50%).\n" +
                "Рекомендуем в будущем строить города рядом с реками или озёрами.";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showCityViewTutorial() {
        if (tutorialShownCityView || uiManager == null) return;
        tutorialShownCityView = true;
        String title = "🏙️ Управление городом";
        String message = "В этом окне вы можете управлять городом:\n" +
                "🍖 Еда – определяет рост населения (избыток -> рост, дефицит -> убыль).\n" +
                "⚙️ Производство – используется для строительства юнитов, улучшений, районов и проектов.\n" +
                "👤 Население – растёт при избытке еды, сокращается при голоде.\n" +
                "😊 Довольство – влияет на стабильность и бонусы.\n" +
                "🔧 Улучшения (фермы, рудники) повышают сбор ресурсов, но требуют рабочих.\n" +
                "🏗️ Районы дают постоянные бонусы к науке, культуре и т.д.\n" +
                "📈 Рост населения ускоряется при хорошем балансе еды и наличии пресной воды.";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showFatigueTutorial() {
        if (tutorialShownFatigue || uiManager == null) return;
        tutorialShownFatigue = true;
        String title = "😩 Механика усталости";
        String message = "Каждый юнит имеет уровень усталости (0-100%).\n" +
                "Усталость растёт при движении и просто с течением времени.\n" +
                "Если усталость превышает 80%, юнит начинает терять членов отряда или население.\n" +
                "Чтобы снизить усталость, используйте кнопку 'Отдохнуть' – это восстанавливает силы.\n" +
                "Следите за усталостью, чтобы не потерять юнитов!";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showHousingTutorial() {
        if (tutorialShownHousing || uiManager == null) return;
        tutorialShownHousing = true;
        String title = "🏠 Жильё и довольство";
        String message = "У каждого города есть лимит жилья (по умолчанию 1000).\n" +
                "Если население превышает лимит, довольство падает.\n" +
                "Чтобы увеличить лимит, постройте ЖИЛИЩА – район, который открывается с технологией 'Примитивное плотничество'.\n" +
                "Жилища требуют размещения на клетке территории и 200 жителей для стройки.\n" +
                "Каждое жилище увеличивает лимит на +500.\n" +
                "Следите за жильём, чтобы поддерживать высокое довольствие!";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showExpansionTutorial() {
        if (tutorialShownExpansion || uiManager == null) return;
        tutorialShownExpansion = true;
        String title = "🌍 Расширение границ";
        String message = "Когда довольство города выше 50, население не превышает лимит жилья и город не голодает,\n" +
                "запускается таймер расширения границы (10 ходов).\n" +
                "В панели города вы видите прогресс таймера.\n" +
                "По его завершении к городу присоединяется случайная соседняя клетка.\n" +
                "Если любое из условий нарушится, таймер сбрасывается.\n" +
                "Расширяйте границы, чтобы получать доступ к новым ресурсам и клеткам для улучшений!";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showCenterImprovementTutorial() {
        if (tutorialShownCenterImprovement || uiManager == null) return;
        tutorialShownCenterImprovement = true;
        String title = "🏛️ Улучшения центра";
        String message = "В списке улучшений доступны центральные улучшения:\n" +
                "🏠 Дом собраний – +1 еда и +1 наука (открывается с Разведением огня).\n" +
                "🏚️ Амбар – +2 еды (открывается с Гончарным делом).\n" +
                "💧 Склад с водой – +2 производства (открывается с Гончарным делом).\n" +
                "🧱 Частокол – защита (пока не реализована, открывается с Примитивным плотничеством).\n" +
                "Они не требуют клетки, строятся как обычные улучшения через производство.\n" +
                "Добавьте их в очередь, и они будут построены, как и другие улучшения!";
        uiManager.showAdvisorMessage(title, message, null);
    }

    public void showTechComplete(String message) {
        if (uiManager == null) return;
        String title = "🔬 Открытие!";
        uiManager.showAdvisorMessage(title, message, null);
    }
    public boolean isSettlerTutorialShown() {
        return tutorialShownSettler;
    }
}
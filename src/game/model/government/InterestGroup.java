package game.model.government;

import java.util.ArrayList;
import java.util.List;

public class InterestGroup {
    private final String name;
    private final String description;
    private int loyalty; // 0-100
    private final List<String> demands; // чего хотят
    private final List<String> tasks; // задания, которые они дают

    public InterestGroup(String name, String description, int initialLoyalty) {
        this.name = name;
        this.description = description;
        this.loyalty = Math.max(0, Math.min(100, initialLoyalty));
        this.demands = new ArrayList<>();
        this.tasks = new ArrayList<>();
        initDefaultDemandsAndTasks();
    }

    private void initDefaultDemandsAndTasks() {
        switch (name) {
            case "Старейшины":
                demands.add("Уважение традиций");
                demands.add("Сохранение легитимности");
                tasks.add("Принять закон 'Общинные собрания'");
                tasks.add("Повысить легитимность до 60");
                break;
            case "Воины":
                demands.add("Сильная армия");
                demands.add("Военные законы");
                tasks.add("Построить 3 боевых юнита");
                tasks.add("Принять закон 'Военный налог'");
                break;
            case "Ремесленники":
                demands.add("Развитие производства");
                demands.add("Свободная торговля");
                tasks.add("Построить 2 улучшения (рудник, лесопилка)");
                tasks.add("Принять закон 'Налог на торговлю'");
                break;
            default:
                break;
        }
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getLoyalty() { return loyalty; }
    public void setLoyalty(int loyalty) {
        this.loyalty = Math.max(0, Math.min(100, loyalty));
    }
    public void addLoyalty(int amount) {
        setLoyalty(this.loyalty + amount);
    }
    public List<String> getDemands() { return demands; }
    public List<String> getTasks() { return tasks; }
}
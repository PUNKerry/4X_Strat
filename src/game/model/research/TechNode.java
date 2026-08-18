package game.model.research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechNode {
    private final String name;
    private final String description;
    private final int cost;
    private final List<String> prerequisites = new ArrayList<>();
    private boolean researched = false;
    private final String advisorMessage;
    private final String bonusDescription;
    private final Map<String, Integer> poleRequirements = new HashMap<>();

    public TechNode(String name, String description, int cost, String bonusDescription, String advisorMessage, String... prerequisites) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.bonusDescription = bonusDescription;
        this.advisorMessage = advisorMessage;
        for (String p : prerequisites) this.prerequisites.add(p);
    }

    public void addPoleRequirement(String pole, int threshold) {
        poleRequirements.put(pole, threshold);
    }

    // Геттеры
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCost() { return cost; }
    public List<String> getPrerequisites() { return prerequisites; }
    public boolean isResearched() { return researched; }
    public void setResearched(boolean researched) { this.researched = researched; }
    public String getAdvisorMessage() { return advisorMessage; }
    public String getBonusDescription() { return bonusDescription; }
    public Map<String, Integer> getPoleRequirements() { return poleRequirements; }
}
package engine.infrastructure;

import java.util.LinkedList;
import java.util.List;

public class HistoryTracker {
    private static final int MAX_HISTORY = 10;
    private final LinkedList<Integer> scienceHistory = new LinkedList<>();
    private final LinkedList<Integer> cultureHistory = new LinkedList<>();
    private final LinkedList<Integer> faithHistory = new LinkedList<>();

    public void addEntry(int science, int culture, int faith) {
        scienceHistory.add(science);
        cultureHistory.add(culture);
        faithHistory.add(faith);
        if (scienceHistory.size() > MAX_HISTORY) scienceHistory.removeFirst();
        if (cultureHistory.size() > MAX_HISTORY) cultureHistory.removeFirst();
        if (faithHistory.size() > MAX_HISTORY) faithHistory.removeFirst();
    }

    public List<Integer> getScienceHistory() { return scienceHistory; }
    public List<Integer> getCultureHistory() { return cultureHistory; }
    public List<Integer> getFaithHistory() { return faithHistory; }

    public void clear() {
        scienceHistory.clear();
        cultureHistory.clear();
        faithHistory.clear();
    }
}
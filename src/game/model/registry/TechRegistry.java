package game.model.registry;

import game.model.research.TechNode;
import game.model.research.TechTree;

import java.util.List;

/**
 * Упрощённый доступ к технологиям через TechTree.
 * Можно расширить для динамической загрузки.
 */
public class TechRegistry {
    private final TechTree techTree;

    public TechRegistry(TechTree techTree) {
        this.techTree = techTree;
    }

    public List<TechNode> getAllTechs() {
        return techTree.getTechs();
    }

    public List<TechNode> getAllSocials() {
        return techTree.getSocials();
    }

    public List<TechNode> getAllReligions() {
        return techTree.getReligions();
    }

    public TechNode getByName(String name) {
        return techTree.getNodeByName(name);
    }

    public boolean isResearched(String name) {
        return techTree.isResearched(name);
    }
}
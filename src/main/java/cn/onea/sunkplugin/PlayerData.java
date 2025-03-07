package cn.onea.sunkplugin;

import org.bukkit.util.Vector;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerData {
    private final Queue<Vector> movementHistory = new LinkedList<>();
    private static final int MAX_HISTORY = 60;

    public void addMovement(Vector vector) {
        if (movementHistory.size() >= MAX_HISTORY) {
            movementHistory.poll();
        }
        movementHistory.add(vector.clone());
    }

    public Queue<Vector> getMovementHistory() {
        return new LinkedList<>(movementHistory);
    }
}
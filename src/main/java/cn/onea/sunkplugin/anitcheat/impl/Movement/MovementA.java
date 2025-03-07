package cn.onea.sunkplugin.anitcheat.impl.Movement;

import cn.onea.sunkplugin.PlayerData;
import cn.onea.sunkplugin.impl.message.AlertManager;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Queue;

public class MovementA implements Listener {
    private static final double VARIANCE_THRESHOLD = 0.015;
    private static final PlayerData data = new PlayerData();
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Vector movement = event.getTo().toVector().subtract(event.getFrom().toVector());
        if (checkUnnaturalAcceleration(data.getMovementHistory())) {
            flagSuspicious(player);
        }
    }

    private boolean checkUnnaturalAcceleration(Queue<Vector> history) {
        double[] speeds = history.stream()
                .mapToDouble(Vector::length)
                .toArray();

        double variance = new Variance().evaluate(speeds);
        return variance < VARIANCE_THRESHOLD;
    }

    private void flagSuspicious(Player player) {
        // 记录可疑行为
        AlertManager.logAction(player, "MovementA");
    }
}

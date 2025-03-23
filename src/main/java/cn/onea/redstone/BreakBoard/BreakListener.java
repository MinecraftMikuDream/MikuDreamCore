package cn.onea.redstone.BreakBoard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BreakListener implements Listener {
    private final BreakBoardManager manager;

    public BreakListener(BreakBoardManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (manager.isValidTool(tool)) {
            manager.updateBreakCount(player, tool);
            if (manager.isBoardEnabled(player.getUniqueId())) {
                manager.updateScoreboard(player);
            }
        }
    }
}
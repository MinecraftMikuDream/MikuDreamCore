package cn.mikudream.core.feature.playermarket.listener;

import cn.mikudream.core.feature.playermarket.gui.PlayerMarketGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;

public class PlayerMarketListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // 使用模式匹配
        switch (Objects.requireNonNull(event.getInventory().getHolder())) {
            case PlayerMarketGUI playerMarketGUI -> {
                playerMarketGUI.onInventoryClick(event);
                event.setCancelled(true);
            }
            default -> {}
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        boolean handled = switch (Objects.requireNonNull(player.getOpenInventory().getTopInventory().getHolder())) {
            case PlayerMarketGUI playerMarketGUI ->
                    playerMarketGUI.handleChatInteraction(player, message);
            default -> false;
        };

        if (handled) {
            event.setCancelled(true);
        }
    }
}
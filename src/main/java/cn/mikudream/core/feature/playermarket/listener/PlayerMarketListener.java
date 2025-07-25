package cn.mikudream.core.feature.playermarket.listener;

import cn.mikudream.core.feature.playermarket.gui.PlayerMarketGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerMarketListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getInventory().getHolder() instanceof PlayerMarketGUI playerMarketGUI) {
            playerMarketGUI.onInventoryClick(event);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        boolean handledPurchase = PlayerMarketGUI.handlePlayerMarketPurchase(player, message);
        boolean handledAddition = PlayerMarketGUI.handlePlayerMarketAddition(player, message);

        if (handledPurchase || handledAddition) {
            event.setCancelled(true);
        }
    }
}
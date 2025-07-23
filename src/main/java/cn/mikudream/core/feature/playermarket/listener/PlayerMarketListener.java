package cn.mikudream.core.feature.playermarket.listener;

import cn.mikudream.core.feature.playermarket.gui.PlayerMarketGUI;
import cn.mikudream.core.feature.shop.gui.AdminShopGUI;
import cn.mikudream.core.feature.shop.gui.ShopGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;

public class PlayerMarketListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof PlayerMarketGUI playerMarketGUI) {
            playerMarketGUI.onInventoryClick(event);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (PlayerMarketGUI.handlePlayerMarketPurchase(player, message)) {
            event.setCancelled(true);
            return;
        }

        if (PlayerMarketGUI.handlePlayerMarketAddition(player, message)) {
            event.setCancelled(true);
            return;
        }
    }
}
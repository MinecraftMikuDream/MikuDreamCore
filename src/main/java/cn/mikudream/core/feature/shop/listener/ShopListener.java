package cn.mikudream.core.feature.shop.listener;

import cn.mikudream.core.feature.playermarket.gui.PlayerMarketGUI;
import cn.mikudream.core.feature.shop.gui.AdminShopGUI;
import cn.mikudream.core.feature.shop.gui.ShopGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;

public class ShopListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof ShopGUI shopGUI) {
            shopGUI.onInventoryClick(event);
        }
        else if (event.getInventory().getHolder() instanceof AdminShopGUI adminShopGUI) {
            adminShopGUI.onInventoryClick(event);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (ShopGUI.handlePurchase(player, message)) {
            event.setCancelled(true);
            return;
        }

        if (AdminShopGUI.handleAdminCommand(player, message)) {
            event.setCancelled(true);
            return;
        }

        if (ShopGUI.handleSale(player, message)) {
            event.setCancelled(true);
        }
    }
}
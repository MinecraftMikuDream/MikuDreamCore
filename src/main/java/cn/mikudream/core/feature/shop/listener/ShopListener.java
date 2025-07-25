package cn.mikudream.core.feature.shop.listener;

import cn.mikudream.core.feature.shop.gui.AdminShopGUI;
import cn.mikudream.core.feature.shop.gui.ShopGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        boolean handlePurchase = ShopGUI.handlePurchase(player, message);
        boolean handleAdminCommand = AdminShopGUI.handleAdminCommand(player, message);
        boolean handleSale = ShopGUI.handleSale(player, message);

        if (handlePurchase || handleAdminCommand || handleSale) {
            event.setCancelled(true);
        }
    }
}
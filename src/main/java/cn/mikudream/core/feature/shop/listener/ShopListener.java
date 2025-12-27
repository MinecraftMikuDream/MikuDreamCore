package cn.mikudream.core.feature.shop.listener;

import cn.mikudream.core.feature.shop.gui.AdminShopGUI;
import cn.mikudream.core.feature.shop.gui.ShopGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jspecify.annotations.NonNull;

public class ShopListener implements Listener {

    @EventHandler
    public void onInventoryClick(@NonNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        boolean handled = switch (event.getInventory().getHolder()) {
            case ShopGUI shopGUI -> {
                shopGUI.onInventoryClick(event);
                yield true;
            }
            case AdminShopGUI adminShopGUI -> {
                adminShopGUI.onInventoryClick(event);
                yield true;
            }
            case null, default -> false;
        };

        if (handled) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        boolean handled = switch (player.getOpenInventory().getTopInventory().getHolder()) {
            case ShopGUI shopGUI -> shopGUI.handleChatInteraction(player, message);
            case AdminShopGUI adminShopGUI -> adminShopGUI.handleChatInteraction(player, message);
            case null, default -> false;
        };

        if (handled) {
            event.setCancelled(true);
        }
    }
}
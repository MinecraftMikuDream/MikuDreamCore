package cn.mikudream.core.Fixes;

import org.bukkit.event.Listener;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class CopyDupe implements Listener {
    private final Set<UUID> readyThrow = new HashSet<UUID>();
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player)((Object)event.getEntity().getShooter());
            this.readyThrow.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (!(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK || item.getType() != Material.TRIDENT || item.containsEnchantment(Enchantment.RIPTIDE))) {
            this.readyThrow.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event,PlayerItemHeldEvent playerItemHeldEvent) {
        HumanEntity humanEntity = event.getWhoClicked();
        Player player = playerItemHeldEvent.getPlayer();
        if (this.readyThrow.contains(humanEntity.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());
        if (itemStack == null || itemStack.getType() != Material.TRIDENT) {
            this.readyThrow.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.readyThrow.remove(event.getPlayer().getUniqueId());
    }
}

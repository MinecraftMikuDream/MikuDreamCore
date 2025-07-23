package cn.mikudream.core.feature.shop.command;

import cn.mikudream.core.feature.shop.gui.AdminShopGUI;
import cn.mikudream.core.feature.shop.gui.ShopGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("shop.admin")) {
                player.sendMessage(ChatColor.RED + "你没有管理商店的权限");
                return true;
            }
            player.openInventory(new AdminShopGUI(player).getInventory());
            return true;
        }

        // 打开普通商店GUI
        player.openInventory(new ShopGUI(player).getInventory());
        return true;
    }
}
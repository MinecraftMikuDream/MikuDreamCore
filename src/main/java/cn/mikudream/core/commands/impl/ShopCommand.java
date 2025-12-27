package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.PlayerCommand;
import cn.mikudream.core.feature.shop.gui.AdminShopGUI;
import cn.mikudream.core.feature.shop.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * 商店命令
 */
public class ShopCommand extends PlayerCommand {
    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public String getDescriptionText() {
        return "商店系统";
    }

    @Override
    public String getUsage() {
        return "/shop [admin]";
    }

    @Override
    protected boolean executePlayer(Player player, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("shop.admin")) {
                player.sendMessage("§c你没有管理商店的权限!");
                return true;
            }
            player.openInventory(new AdminShopGUI(player).getInventory());
        } else {
            player.openInventory(new ShopGUI(player).getInventory());
        }

        return true;
    }

    @Override
    protected Optional<List<String>> tabCompletePlayer(Player player, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = List.of();
            if (player.hasPermission("shop.admin")) {
                suggestions = List.of("admin");
            }
            return Optional.of(suggestions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList());
        }

        return Optional.empty();
    }
}
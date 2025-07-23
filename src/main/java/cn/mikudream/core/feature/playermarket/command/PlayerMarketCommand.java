package cn.mikudream.core.feature.playermarket.command;

import cn.mikudream.core.feature.playermarket.gui.PlayerMarketGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerMarketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此命令");
            return true;
        }

        int page = 0;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
                if (page < 0) page = 0;
            } catch (NumberFormatException e) {
                player.sendMessage("页码必须是一个整数");
                return true;
            }
        }

        player.openInventory(new PlayerMarketGUI(player, page).getInventory());
        return true;
    }
}
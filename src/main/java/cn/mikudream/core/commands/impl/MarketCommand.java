package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.PlayerCommand;
import cn.mikudream.core.feature.playermarket.gui.PlayerMarketGUI;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * 玩家市场命令
 */
public class MarketCommand extends PlayerCommand {
    @Override
    public String getName() {
        return "market";
    }

    @Override
    public String getDescriptionText() {
        return "玩家市场";
    }

    @Override
    public String getUsage() {
        return "/market [页码]";
    }

    @Override
    protected boolean executePlayer(Player player, Command command, String label, String[] args) {
        int page = 0;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
                if (page < 0) page = 0;
            } catch (NumberFormatException e) {
                player.sendMessage("§c页码必须是有效的数字!");
                return true;
            }
        }

        player.openInventory(new PlayerMarketGUI(player, page).getInventory());
        return true;
    }
}
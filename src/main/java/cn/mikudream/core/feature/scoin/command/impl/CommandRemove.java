package cn.mikudream.core.feature.scoin.command.impl;

import cn.mikudream.core.feature.scoin.SCoinManager;
import cn.mikudream.core.feature.scoin.command.CommandInfo;
import cn.mikudream.core.feature.scoin.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandInfo(
        name = "remove",
        purpose = "remove scoin"
)
public class CommandRemove extends SubCommand {
    private int amount;
    public SCoinManager coinManager;

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("scoin.remove"))
        {
            sender.sendMessage("§c你没有权限！");
            return false;
        }

        if(args.length < 2)
        {
            sender.sendMessage("§c请输入玩家名称！");
            return false;
        }
        String playerName = args[1];

        try {
            amount = Integer.parseInt(args[2]);
            if(amount < 0)
            {
                sender.sendMessage("§c数量必须是正数！");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数量必须是整数！");
            return false;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§c玩家 " + playerName + " 不存在或不在线！");
            return false;
        }

        UUID uuid = target.getUniqueId();
        coinManager.removeCoins(uuid, amount);
        sender.sendMessage("§a已为玩家 " + playerName + " 减少 " + amount + " sunk币");
        target.sendMessage("§e你失去了 " + amount + " sunk币");
        return true;
    }
}

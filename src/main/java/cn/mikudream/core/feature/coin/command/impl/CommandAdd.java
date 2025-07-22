package cn.mikudream.core.feature.coin.command.impl;

import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.coin.command.CoinCommandInfo;
import cn.mikudream.core.feature.coin.command.CoinSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CoinCommandInfo(name="add", purpose="Add Coin", syntax="<player> <amount>")
public class CommandAdd extends CoinSubCommand {
    int amount;
    public CoinsManager coinManager;

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§6用法: /coin " + "add" + " <玩家> <数量>");
            return true;
        }
        String playerName = args[1];

        try {
            amount = Integer.parseInt(args[2]);
            if(amount < 0)
            {
                sender.sendMessage("§c数量必须是正数！");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数量必须是整数！");
            return true;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§c玩家 " + playerName + " 不存在或不在线！");
            return true;
        }
        UUID uuid = target.getUniqueId();

        coinManager.addCoins(uuid, amount);
        sender.sendMessage("§a已为玩家 " + playerName + " 增加 " + amount + " sunk币");
        target.sendMessage("§e你获得了 " + amount + " sunk币");
        return true;
    }
}

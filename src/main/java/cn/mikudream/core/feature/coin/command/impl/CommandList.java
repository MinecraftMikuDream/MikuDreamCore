package cn.mikudream.core.feature.coin.command.impl;

import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.coin.command.CoinCommandInfo;
import cn.mikudream.core.feature.coin.command.CoinSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CoinCommandInfo(name="list", purpose="List SCoin")
public class CommandList extends CoinSubCommand {
    private static final String PERMISSION = "coin.admin";
    public CoinsManager coinManager;

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args[0].equalsIgnoreCase("list")) {
            if (args.length >= 2) {
                if (!sender.hasPermission(PERMISSION)) {
                    sender.sendMessage("§c你只能查看自己的余额！");
                    return true;
                }
                // 查询他人
                String targetName = args[1];
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    sender.sendMessage("§c玩家不存在！");
                    return true;
                }
                int coins = coinManager.getCoins(target.getUniqueId());
                sender.sendMessage("§e玩家 " + targetName + " 当前有 " + coins + " coins");
            } else {
                // 查询自己
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c控制台不能查看coin！");
                    return true;
                }
                int coins = coinManager.getCoins(player.getUniqueId());
                sender.sendMessage("§e你的coin为：" + coins);
            }
        }
        return true;
    }
}

package cn.mikudream.core.feature.scoin.command.impl;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.scoin.SCoinManager;
import cn.mikudream.core.feature.scoin.command.CommandInfo;
import cn.mikudream.core.feature.scoin.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name="list", purpose="List SCoin")
public class CommandList extends SubCommand {
    private static final String PERMISSION = "sunk.coin.admin";
    public SCoinManager coinManager;
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
                sender.sendMessage("§e玩家 " + targetName + " 当前有 " + coins + " sunk币");
            } else {
                // 查询自己
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c控制台不能查看余额！");
                    return true;
                }
                int coins = coinManager.getCoins(player.getUniqueId());
                sender.sendMessage("§e你的sunk币余额为：" + coins);
            }
        }
        return true;
    }
}

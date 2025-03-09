package cn.onea.sunkplugin.command;

import cn.onea.sunkplugin.CoinManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class CoinCommand implements CommandExecutor {
    private final CoinManager coinManager;
    private static final String PERMISSION = "sunk.coin.admin";

    public CoinCommand(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        // 通用参数检查
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            return handleList(sender, args);
        }

        // 权限检查
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§c你没有权限管理金币！");
            return true;
        }

        // 参数解析
        if (args.length < 3) {
            sender.sendMessage("§6用法: /" + label + " <玩家> <add/del> <数量>");
            return true;
        }

        String playerName = args[0];
        String operation = args[1];
        int amount;

        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数量必须是整数！");
            return true;
        }

        // 获取目标玩家
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§c玩家 " + playerName + " 不存在或不在线！");
            return true;
        }
        UUID uuid = target.getUniqueId();

        // 执行操作
        switch (operation.toLowerCase()) {
            case "add":
                coinManager.addCoins(uuid, amount);
                sender.sendMessage("§a已为玩家 " + playerName + " 增加 " + amount + " sunk币");
                target.sendMessage("§e你获得了 " + amount + " sunk币");
                break;
            case "del":
                coinManager.removeCoins(uuid, amount);
                sender.sendMessage("§c已扣除玩家 " + playerName + " " + amount + " sunk币");
                target.sendMessage("§e你的sunk币减少了 " + amount);
                break;
            default:
                sender.sendMessage("§c无效操作，请使用 add 或 del");
        }

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        // 检查是否为OP或玩家查询自己
        if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage("§c你只能查看自己的余额！");
                return true;
            }
            if (args.length >= 2) {
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
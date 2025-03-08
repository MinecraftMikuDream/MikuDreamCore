package cn.onea.sunkplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static cn.onea.sunkplugin.SunkPlugins.featureEnabled;

public class SKill implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return false;
        }
        if (!featureEnabled) {
            player.sendMessage("§c当前自杀功能已被管理员关闭喵！");
            return false;
        }
        // 玩家自杀
        player.setHealth(0.0);
        player.sendMessage("§6你已成功自杀喵");
        Bukkit.broadcastMessage("§e玩家 " + player.getName() + " 选择了自我了断喵，可惜捏");
        return false;
    }
}

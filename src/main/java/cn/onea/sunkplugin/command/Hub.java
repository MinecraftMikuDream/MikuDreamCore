package cn.onea.sunkplugin.command;

import cn.onea.sunkplugin.SunkPlugins;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Hub implements CommandExecutor {
    SunkPlugins sunkPlugins = new SunkPlugins();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return false;
        }
        // 使用配置中的 home 坐标
        String command_1 = "execute in minecraft:the_void run minecraft:tp " + player.getName() + " " + sunkPlugins.homex + " " + sunkPlugins.homey + " " + sunkPlugins.homez;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command_1);
        // 设置玩家为冒险模式
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage("§6传送成功！你已进入主城，当前游戏模式为冒险。");
        return false;
    }
}

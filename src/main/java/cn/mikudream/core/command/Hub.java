package cn.mikudream.core.command;

import cn.mikudream.core.MikuDream;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Hub implements CommandExecutor {
    private final MikuDream plugin;

    public Hub(MikuDream plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return false;
        }
        // 使用配置中的 home 坐标
        String command_1 = "execute in " + plugin + " run minecraft:tp " + player.getName() + " " + plugin.lobby_x + " " + plugin.lobby_y + " " + plugin.lobby_z;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command_1);
        // 设置玩家为冒险模式
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage("§6传送成功！你已进入主城，当前游戏模式为冒险。");
        return false;
    }
}

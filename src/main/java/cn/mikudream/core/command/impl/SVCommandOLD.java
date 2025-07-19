package cn.mikudream.core.command.impl;

import cn.mikudream.core.MikuDream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SVCommandOLD implements CommandExecutor {
    public static final Set<UUID> invisiblePlayers = new HashSet<>();
    private final MikuDream plugin = MikuDream.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // 检查发送者是否是玩家
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令!");
            return false;
        }

        if (!sender.hasPermission("sunk.sv")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令!");
            return false;
        }

        if(!((Player) sender).isInvisible())
        {
            ((Player) sender).setInvisible(true);
            invisiblePlayers.add(((Player) sender).getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "你已隐身");
        }
        else
        {
            ((Player) sender).setInvisible(false);
            invisiblePlayers.remove(((Player) sender).getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "你已可见");
        }
        return true;
    }
}

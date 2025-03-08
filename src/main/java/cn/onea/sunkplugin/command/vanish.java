package cn.onea.sunkplugin.command;

import cn.onea.sunkplugin.SunkPlugins;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class vanish implements CommandExecutor {
    private final SunkPlugins plugin;

    public vanish(SunkPlugins plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }

        if (!player.hasPermission("sunk.vanish")) {
            player.sendMessage("§c权限不足！");
            return true;
        }

        if (player.isInvisible()) {
            // 取消隐身
            player.setInvisible(false);
            player.setDisplayName(player.getName().replace("隐身 ", ""));
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
            player.sendMessage("§a你已取消隐身");
        } else {
            // 隐身
            player.setInvisible(true);
            player.setDisplayName("隐身 " + player.getName());
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, player));
            player.sendMessage("§a你已隐身");
        }
        return true;
    }
}

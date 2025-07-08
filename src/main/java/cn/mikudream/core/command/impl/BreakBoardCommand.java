package cn.mikudream.core.command.impl;

import cn.mikudream.core.feature.breakboard.BreakBoardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BreakBoardCommand implements CommandExecutor {
    private final BreakBoardManager manager;

    public BreakBoardCommand(BreakBoardManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        manager.toggleBoard(player);
        boolean isEnabled = manager.isBoardEnabled(player.getUniqueId());
        player.sendMessage("§a挖掘榜已" + (isEnabled ? "开启" : "关闭"));
        return true;
    }
}
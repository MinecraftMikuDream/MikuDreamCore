package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.PlayerCommand;
import cn.mikudream.core.MikuDream;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * 回城命令
 */
public class HubCommand extends PlayerCommand {
    private final MikuDream plugin;

    public HubCommand(MikuDream plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "hub";
    }

    @Override
    public String getDescriptionText() {
        return "传送回主城";
    }

    @Override
    protected boolean executePlayer(Player player, Command command, String label, String[] args) {
        // 传送回主城
        String tpCommand = "execute in " + plugin.lobby_world +
                " run tp " + player.getName() + " " +
                plugin.lobby_x + " " + plugin.lobby_y + " " + plugin.lobby_z;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tpCommand);
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage("§a已传送回主城!");

        return true;
    }

    @Override
    protected Optional<List<String>> tabCompletePlayer(Player player, Command command, String label, String[] args) {
        return Optional.empty();
    }
}
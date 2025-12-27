package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.AdminCommand;
import cn.mikudream.core.MikuDream;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 隐身命令
 */
public class InvisibleCommand extends AdminCommand {
    private final MikuDream plugin;
    private final Set<UUID> invisiblePlayers;

    public InvisibleCommand(MikuDream plugin, Set<UUID> invisiblePlayers) {
        this.plugin = plugin;
        this.invisiblePlayers = invisiblePlayers;
    }

    @Override
    public String getName() {
        return "sv";
    }

    @Override
    public String getDescriptionText() {
        return "隐身命令";
    }

    @Override
    public String getUsage() {
        return "/sv";
    }

    @Override
    public String getPermission() {
        return "sunk.sv";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public boolean executeAdmin(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (invisiblePlayers.contains(playerId)) {
            // 解除隐身
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
            invisiblePlayers.remove(playerId);
            player.sendMessage("§a你已解除隐身状态!");
        } else {
            // 进入隐身
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }
            invisiblePlayers.add(playerId);
            player.sendMessage("§a你已进入隐身状态!");
        }

        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Optional.empty();
    }
}
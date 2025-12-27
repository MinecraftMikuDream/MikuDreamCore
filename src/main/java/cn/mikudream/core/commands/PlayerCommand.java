package cn.mikudream.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * 玩家命令抽象基类
 * 所有只有玩家可以执行的命令应继承此类
 */
public abstract class PlayerCommand implements BaseCommand {

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public final boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return true;
        }
        return executePlayer((Player) sender, command, label, args);
    }

    /**
     * 玩家命令执行方法
     */
    protected abstract boolean executePlayer(Player player, Command command, String label, String[] args);

    @Override
    public Optional<List<String>> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return Optional.empty();
        }
        return tabCompletePlayer(player, command, label, args);
    }

    /**
     * 玩家命令Tab补全
     */
    protected Optional<List<String>> tabCompletePlayer(Player player, Command command, String label, String[] args) {
        return Optional.empty();
    }
}
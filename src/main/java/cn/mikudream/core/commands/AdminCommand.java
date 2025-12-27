package cn.mikudream.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

/**
 * 管理员命令抽象基类
 * 需要特定权限的命令应继承此类
 */
public abstract class AdminCommand implements BaseCommand {

    /**
     * 获取所需权限
     */
    @Override
    public abstract String getPermission();

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return true;
        }
        return executeAdmin(sender, command, label, args);
    }

    /**
     * 管理员命令执行方法
     */
    protected abstract boolean executeAdmin(CommandSender sender, Command command, String label, String[] args);

    @Override
    public Optional<List<String>> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return Optional.empty();
        }
        return tabCompleteAdmin(sender, command, label, args);
    }

    /**
     * 管理员命令Tab补全
     */
    protected Optional<List<String>> tabCompleteAdmin(CommandSender sender, Command command, String label, String[] args) {
        return Optional.empty();
    }
}
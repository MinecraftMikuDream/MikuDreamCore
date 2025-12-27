package cn.mikudream.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

/**
 * 基础命令接口
 * 所有命令必须实现此接口
 */
public interface BaseCommand {

    /**
     * 执行命令
     */
    boolean execute(CommandSender sender, Command command, String label, String[] args);

    /**
     * Tab补全建议
     */
    Optional<List<String>> tabComplete(CommandSender sender, Command command, String label, String[] args);

    /**
     * 获取命令描述
     */
    default CommandDescription getDescription() {
        return new CommandDescription(getName(), getUsage(), getDescriptionText(), getPermission());
    }

    /**
     * 命令名称
     */
    String getName();

    /**
     * 命令用法
     */
    default String getUsage() {
        return "/" + getName();
    }

    /**
     * 命令描述
     */
    default String getDescriptionText() {
        return "一个命令";
    }

    /**
     * 所需权限（null表示无需权限）
     */
    default String getPermission() {
        return null;
    }

    /**
     * 是否只有玩家可以执行
     */
    default boolean isPlayerOnly() {
        return false;
    }

    /**
     * 是否只有控制台可以执行
     */
    default boolean isConsoleOnly() {
        return false;
    }

    /**
     * 验证命令执行者
     */
    default boolean validateSender(CommandSender sender) {
        if (isPlayerOnly() && !(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令!");
            return false;
        }

        if (isConsoleOnly() && sender instanceof org.bukkit.entity.Player) {
            sender.sendMessage("§c只有控制台可以执行此命令!");
            return false;
        }

        String permission = getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendMessage("§c你没有权限执行此命令!");
            return false;
        }

        return true;
    }

    /**
     * 命令描述记录类
     */
    record CommandDescription(
            String name,
            String usage,
            String description,
            String permission
    ) {}
}
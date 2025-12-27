package cn.mikudream.core.commands;

import cn.mikudream.core.MikuDream;
import org.bukkit.command.PluginCommand;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命令注册管理器
 */
public class CommandRegistry {
    private final MikuDream plugin;
    private final Map<String, BaseCommand> commands = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> dependencies = new ConcurrentHashMap<>();

    public CommandRegistry(MikuDream plugin) {
        this.plugin = plugin;
    }

    /**
     * 注册命令
     */
    public void registerCommand(String name, BaseCommand command) {
        commands.put(name.toLowerCase(), command);

        PluginCommand pluginCommand = plugin.getCommand(name);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(new DelegatingCommandExecutor(command));
            pluginCommand.setTabCompleter(new DelegatingTabCompleter(command));
            plugin.getLogger().info("已注册命令: " + name);
        } else {
            plugin.getLogger().warning("无法注册命令: " + name + " - 检查plugin.yml");
        }
    }

    /**
     * 批量注册命令
     */
    public void registerCommands(Map<String, BaseCommand> commands) {
        commands.forEach(this::registerCommand);
    }

    /**
     * 获取已注册的命令
     */
    public Optional<BaseCommand> getCommand(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    /**
     * 获取所有已注册的命令
     */
    public Collection<BaseCommand> getAllCommands() {
        return commands.values();
    }

    /**
     * 注册依赖（供命令使用）
     */
    public <T> void registerDependency(Class<T> type, T instance) {
        dependencies.put(type, instance);
    }

    /**
     * 获取依赖
     */
    @SuppressWarnings("unchecked")
    public <T> T getDependency(Class<T> type) {
        return (T) dependencies.get(type);
    }

    /**
     * 委托命令执行器
     */
    private static class DelegatingCommandExecutor implements org.bukkit.command.CommandExecutor {
        private final BaseCommand command;

        DelegatingCommandExecutor(BaseCommand command) {
            this.command = command;
        }

        @Override
        public boolean onCommand(org.bukkit.command.@NonNull CommandSender sender,
                                 org.bukkit.command.@NonNull Command cmd, @NonNull String label, String[] args) {
            return command.execute(sender, cmd, label, args);
        }
    }

    /**
     * 委托Tab补全器
     */
    private static class DelegatingTabCompleter implements org.bukkit.command.TabCompleter {
        private final BaseCommand command;

        DelegatingTabCompleter(BaseCommand command) {
            this.command = command;
        }

        @Override
        public List<String> onTabComplete(org.bukkit.command.CommandSender sender,
                                          org.bukkit.command.Command cmd, String label, String[] args) {
            return command.tabComplete(sender, cmd, label, args)
                    .orElse(List.of());
        }
    }
}
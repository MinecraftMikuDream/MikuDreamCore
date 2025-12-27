package cn.mikudream.core.commands;

import java.util.*;

/**
 * 支持子命令的命令基类
 */
public abstract class SubCommand extends PlayerCommand {

    private final Map<String, CommandHandler> subCommands = new HashMap<>();
    private CommandHandler defaultHandler = null;

    protected SubCommand() {
        registerSubCommands();
    }

    /**
     * 注册子命令
     */
    protected abstract void registerSubCommands();

    /**
     * 注册子命令处理器
     */
    protected void registerSubCommand(String name, CommandHandler handler) {
        subCommands.put(name.toLowerCase(), handler);
    }

    /**
     * 注册默认处理器（当没有匹配的子命令时调用）
     */
    protected void registerDefaultHandler(CommandHandler handler) {
        this.defaultHandler = handler;
    }

    @Override
    protected boolean executePlayer(org.bukkit.entity.Player player, org.bukkit.command.Command command,
                                    String label, String[] args) {
        if (args.length == 0) {
            if (defaultHandler != null) {
                return defaultHandler.handle(player, args);
            }
            return showHelp(player);
        }

        String subCommand = args[0].toLowerCase();
        CommandHandler handler = subCommands.get(subCommand);

        if (handler != null) {
            return handler.handle(player, Arrays.copyOfRange(args, 1, args.length));
        }

        if (defaultHandler != null) {
            return defaultHandler.handle(player, args);
        }

        return showHelp(player);
    }

    @Override
    protected Optional<List<String>> tabCompletePlayer(org.bukkit.entity.Player player,
                                                       org.bukkit.command.Command command,
                                                       String label, String[] args) {
        if (args.length == 1) {
            // 建议子命令名称
            List<String> suggestions = new ArrayList<>(subCommands.keySet());
            return Optional.of(suggestions.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .toList());
        }

        if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            CommandHandler handler = subCommands.get(subCommand);
            if (handler instanceof TabCompleter completer) {
                return completer.onTabComplete(player, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return Optional.empty();
    }

    /**
     * 显示帮助信息
     */
    protected boolean showHelp(org.bukkit.entity.Player player) {
        player.sendMessage("§6§l" + getName() + " 命令帮助");
        player.sendMessage("§7用法: " + getUsage());

        if (!subCommands.isEmpty()) {
            player.sendMessage("§e可用子命令:");
            subCommands.forEach((name, handler) -> {
                String description = handler.getDescription() != null ?
                        " §7- " + handler.getDescription() : "";
                player.sendMessage("  §6/" + getName() + " " + name + description);
            });
        }

        return true;
    }

    /**
     * 命令处理器接口
     */
    @FunctionalInterface
    public interface CommandHandler {
        boolean handle(org.bukkit.entity.Player player, String[] args);

        default String getDescription() {
            return null;
        }
    }

    /**
     * 支持Tab补全的命令处理器
     */
    public interface TabCompleter extends CommandHandler {
        Optional<List<String>> onTabComplete(org.bukkit.entity.Player player, String[] args);
    }
}
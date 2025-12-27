package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.AdminCommand;
import cn.mikudream.core.MikuDream;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

/**
 * 配置管理命令
 */
public class ConfigCommands extends AdminCommand {
    private final MikuDream plugin;

    public ConfigCommands(MikuDream plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "catconfig";
    }

    @Override
    public String getDescriptionText() {
        return "配置管理命令";
    }

    @Override
    public String getUsage() {
        return "/catconfig set <lobby|skill> [参数]";
    }

    @Override
    public String getPermission() {
        return "mikucore.config";
    }

    @Override
    public boolean executeAdmin(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return true;
        }

        if (args.length < 2) {
            return showHelp(sender);
        }

        if (!args[0].equalsIgnoreCase("set")) {
            return showHelp(sender);
        }

        return switch (args[1].toLowerCase()) {
            case "lobby" -> handleLobbyConfig(sender, args);
            case "skill" -> handleSkillConfig(sender, args);
            default -> showHelp(sender);
        };
    }

    @Override
    public Optional<List<String>> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return Optional.empty();
        }

        if (args.length == 1) {
            List<String> suggestions = List.of("set");
            return Optional.of(suggestions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            List<String> suggestions = List.of("lobby", "skill");
            return Optional.of(suggestions.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList());
        }

        return Optional.empty();
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("§6§l配置管理命令");
        sender.sendMessage("§e/catconfig set lobby <x> <y> <z> §7- 设置主城坐标");
        sender.sendMessage("§e/catconfig set skill <true|false> §7- 开关自杀功能");
        return true;
    }

    private boolean handleLobbyConfig(CommandSender sender, String[] args) {
        if (args.length != 5) {
            sender.sendMessage("§c用法: /catconfig set lobby <x> <y> <z>");
            return true;
        }

        try {
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);

            plugin.updatelobby_xyz(x, y, z);
            sender.sendMessage("§a已将主城坐标设置为: " + x + ", " + y + ", " + z);

        } catch (NumberFormatException e) {
            sender.sendMessage("§c坐标必须是整数!");
        }

        return true;
    }

    private boolean handleSkillConfig(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("§c用法: /catconfig set skill <true|false>");
            return true;
        }

        String value = args[2].toLowerCase();
        if (!value.equals("true") && !value.equals("false")) {
            sender.sendMessage("§c参数必须是 true 或 false!");
            return true;
        }

        boolean enabled = Boolean.parseBoolean(value);
        MikuDream.skill_Enabled = enabled;
        plugin.setskill(enabled, sender);

        sender.sendMessage("§a自杀功能已" + (enabled ? "启用" : "禁用"));
        return true;
    }
}
package cn.onea.redstone.command;

import cn.onea.redstone.Redstone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static cn.onea.redstone.Redstone.featureEnabled;
import static org.bukkit.Bukkit.getLogger;

public class SConfigCommand implements CommandExecutor {
    private static final String ADMIN_PERMISSION = "sunk.config";
    private final Redstone plugin;
    public SConfigCommand(Redstone plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§c没有权限！");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§6用法: /catconfig set <home|skill> ...");
            return false;
        }
        if (!args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("§6用法: /catconfig set <home|skill> ...");
            return false;
        }

        // 处理 home 坐标修改
        if (args[1].equalsIgnoreCase("home")) {
            if (args.length != 5) {
                sender.sendMessage("§6用法: /catconfig set home <x> <y> <z>");
                return false;
            }
            try {
                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);
                // 更新配置
                plugin.updatehome_xyz(x, y, z);
                sender.sendMessage("§a已将 home 坐标修改为: " + x + ", " + y + ", " + z);
                getLogger().info("Home 坐标更新为: " + x + ", " + y + ", " + z);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("§c坐标必须为整数！");
            }
            return false;
        }

        // 处理 catkill 功能开关修改
        if (args[1].equalsIgnoreCase("skill")) {
            if (args.length != 3) {
                sender.sendMessage("§6用法: /catconfig set skill <true|false>");
                return false;
            }
            String value = args[2].toLowerCase();
            if (value.equals("true") || value.equals("false")) {
                featureEnabled = Boolean.parseBoolean(value);
                // 同步更新配置文件
                plugin.setcatkill(featureEnabled,sender);

            } else {
                sender.sendMessage("§c参数错误，必须为 true 或 false");
            }
            return false;
        }

        sender.sendMessage("§6用法: /catconfig set <home|skill> ...");
        return false;
    }
}

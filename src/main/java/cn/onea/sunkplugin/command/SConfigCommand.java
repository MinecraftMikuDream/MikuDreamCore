package cn.onea.sunkplugin.command;

import cn.onea.sunkplugin.SunkPlugins;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static cn.onea.sunkplugin.SunkPlugins.ADMIN_PERMISSION;
import static cn.onea.sunkplugin.SunkPlugins.featureEnabled;
import static org.bukkit.Bukkit.getLogger;

public class SConfigCommand implements CommandExecutor {
    SunkPlugins sunkPlugins = new SunkPlugins();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§c你没有权限使用该命令！");
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage("§6用法: /catconfig set <home|catkill> ...");
            return false;
        }
        if (!args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("§6用法: /catconfig set <home|catkill> ...");
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
                sunkPlugins.updatehome_xyz(x, y, z);
                sender.sendMessage("§a已将 home 坐标修改为: " + x + ", " + y + ", " + z);
                getLogger().info("Home 坐标更新为: " + x + ", " + y + ", " + z);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c坐标必须为整数！");
            }
            return false;
        }

        // 处理 catkill 功能开关修改
        if (args[1].equalsIgnoreCase("catkill")) {
            if (args.length != 3) {
                sender.sendMessage("§6用法: /catconfig set catkill <true|false>");
                return false;
            }
            String value = args[2].toLowerCase();
            if (value.equals("true") || value.equals("false")) {
                featureEnabled = Boolean.parseBoolean(value);
                // 同步更新配置文件
                sunkPlugins.setcatkill(featureEnabled,sender);

            } else {
                sender.sendMessage("§c参数错误，必须为 true 或 false");
            }
            return false;
        }

        sender.sendMessage("§6用法: /catconfig set <home|catkill> ...");
        return false;
    }
}

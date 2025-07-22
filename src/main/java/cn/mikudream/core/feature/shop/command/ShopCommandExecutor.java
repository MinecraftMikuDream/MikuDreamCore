package cn.mikudream.core.feature.shop.command;

import cn.mikudream.core.feature.shop.command.impl.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ShopCommandExecutor implements CommandExecutor {
    private final List<ShopSubCommand> commands = new ArrayList<>();

    public ShopCommandExecutor() {
        commands.add(new CommandListShop());
        commands.add(new CommandBuyShop());
        commands.add(new CommandSellShop());
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("shop.commands")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "Shop Commands:\n");
            for (ShopSubCommand cmd : commands) {
                sender.sendMessage(ChatColor.GREEN + "用法: /shop " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
                sender.sendMessage(ChatColor.GRAY + " - " + cmd.getInfo().purpose());
            }
            sender.sendMessage("");
            return true;
        }

        String sub = args[0];
        for (ShopSubCommand cmd : commands) {
            if (cmd.getInfo().name().equalsIgnoreCase(sub)) {
                if (!sender.hasPermission("shop." + sub)) {
                    sender.sendMessage(ChatColor.RED + "你没有权限使用此子命令");
                    return true;
                }
                boolean success = cmd.execute(sender, command, label, args);
                if (!success) {
                    sender.sendMessage(ChatColor.RED + "用法: /shop " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
                }
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "未知的子命令，输入 /shop 查看可用命令");
        return true;
    }
}
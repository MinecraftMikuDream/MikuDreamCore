package cn.mikudream.core.feature.shop.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class ShopSubCommand implements Comparable<ShopSubCommand> {

    protected abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    protected void printSeparator(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "&m----------------------------------------------");
    }

    protected void printBlankLine(CommandSender sender) {
        sender.sendMessage("");
    }

    protected void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    public ShopCommandInfo getInfo() {
        ShopCommandInfo info = this.getClass().getAnnotation(ShopCommandInfo.class);
        if (info == null) {
            System.err.println("ShopCommandInfo annotation missing on " + this.getClass().getSimpleName());
        }
        return info;
    }

    public int compareTo(@NotNull ShopSubCommand other) {
        return 0;
    }

}

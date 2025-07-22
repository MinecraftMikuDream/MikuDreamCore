package cn.mikudream.core.feature.coin.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class CoinSubCommand implements Comparable<CoinSubCommand> {

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

    public CoinCommandInfo getInfo() {
        CoinCommandInfo info = this.getClass().getAnnotation(CoinCommandInfo.class);
        if (info == null) {
            System.err.println("CoinCommandInfo annotation missing on " + this.getClass().getSimpleName());
        }
        return info;
    }

    public int compareTo(@NotNull CoinSubCommand other) {
        return 0;
    }

}

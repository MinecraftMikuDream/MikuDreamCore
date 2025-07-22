package cn.mikudream.core.feature.friend.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class FriendSubCommand implements Comparable<FriendSubCommand> {

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

    public FriendCommandInfo getInfo() {
        FriendCommandInfo info = this.getClass().getAnnotation(FriendCommandInfo.class);
        if (info == null) {
            System.err.println("FriendCommandInfo annotation missing on " + this.getClass().getSimpleName());
        }
        return info;
    }

    public int compareTo(@NotNull FriendSubCommand other) {
        return 0;
    }

}

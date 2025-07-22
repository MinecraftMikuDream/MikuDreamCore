package cn.mikudream.core.feature.friend.command;

import cn.mikudream.core.feature.friend.FriendSystem;
import cn.mikudream.core.feature.friend.command.impl.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FriendCommandExecutor implements CommandExecutor {
    private final List<FriendSubCommand> commands = new ArrayList<>();
    private final FriendSystem friendSystem;

    public FriendCommandExecutor(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
        registerCommands();
    }

    private void registerCommands() {
        commands.add(new CommandAdd(friendSystem));
        commands.add(new CommandList(friendSystem));
        commands.add(new CommandRemove(friendSystem));
        commands.add(new CommandAccept(friendSystem));
        commands.add(new CommandDeny(friendSystem));
        commands.add(new CommandTp(friendSystem));
        commands.sort(Comparator.comparing(cmd -> cmd.getInfo().name()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("friend.commands")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "friend Commands:\n");
            for (FriendSubCommand cmd : commands) {
                sender.sendMessage(ChatColor.GREEN + "Use /friend " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            sender.sendMessage("");
            return true;
        }

        String sub = args[0];
        for (FriendSubCommand cmd : commands) {
            String name = cmd.getInfo().name();
            if (!name.equals(sub)) {
                continue;
            }
            boolean success = cmd.execute(sender, command, label, args);
            if (!success) {
                sender.sendMessage(ChatColor.GREEN + "用法: /friend " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            return true;
        }
        return false;
    }
}

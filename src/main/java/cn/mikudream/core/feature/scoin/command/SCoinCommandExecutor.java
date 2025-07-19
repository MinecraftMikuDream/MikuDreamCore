package cn.mikudream.core.feature.scoin.command;

import cn.mikudream.core.feature.scoin.SCoinManager;
import cn.mikudream.core.feature.scoin.command.impl.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class SCoinCommandExecutor implements CommandExecutor {
    private final List<SubCommand> commands = new ArrayList<>();
    private final SCoinManager scoinManager;

    public SCoinCommandExecutor(SCoinManager scoinManager) {
        this.scoinManager = scoinManager;
        commands.add(new CommandVersion());

        CommandAdd addCommand = new CommandAdd();
        addCommand.coinManager = scoinManager;
        commands.add(addCommand);

        CommandList listCommand = new CommandList();
        listCommand.coinManager = scoinManager;
        commands.add(listCommand);

        CommandReload reloadCommand = new CommandReload();
        reloadCommand.coinManager = scoinManager;
        commands.add(reloadCommand);

        CommandRemove removeCommand = new CommandRemove();
        removeCommand.coinManager = scoinManager;
        commands.add(removeCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("scoin.commands")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "SCoin Commands:\n");
            for (SubCommand cmd : commands) {
                sender.sendMessage(ChatColor.GREEN + "Use /SCoin " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            sender.sendMessage("");
            return true;
        }

        String sub = args[0];
        for (SubCommand cmd : commands) {
            String name = cmd.getInfo().name();
            if (!name.equals(sub)) {
                continue;
            }
            boolean success = cmd.execute(sender, command, label, args);
            if (!success) {
                sender.sendMessage(ChatColor.GREEN + "用法: /scoin " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            return true;
        }
        return false;
    }
}

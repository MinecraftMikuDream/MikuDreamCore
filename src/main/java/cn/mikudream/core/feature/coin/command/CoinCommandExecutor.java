package cn.mikudream.core.feature.coin.command;

import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.coin.command.impl.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CoinCommandExecutor implements CommandExecutor {
    private final List<CoinSubCommand> commands = new ArrayList<>();
    private final CoinsManager coinManager;

    public CoinCommandExecutor(CoinsManager coinManager) {
        this.coinManager = coinManager;

        CommandAdd addCommand = new CommandAdd();
        addCommand.coinManager = coinManager;
        commands.add(addCommand);

        CommandList listCommand = new CommandList();
        listCommand.coinManager = coinManager;
        commands.add(listCommand);

        CommandReload reloadCommand = new CommandReload();
        reloadCommand.coinManager = coinManager;
        commands.add(reloadCommand);

        CommandRemove removeCommand = new CommandRemove();
        removeCommand.coinManager = coinManager;
        commands.add(removeCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("coin.commands")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "Coin Commands:\n");
            for (CoinSubCommand cmd : commands) {
                sender.sendMessage(ChatColor.GREEN + "Use /Coin " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            sender.sendMessage("");
            return true;
        }

        String sub = args[0];
        for (CoinSubCommand cmd : commands) {
            String name = cmd.getInfo().name();
            if (!name.equals(sub)) {
                continue;
            }
            boolean success = cmd.execute(sender, command, label, args);
            if (!success) {
                sender.sendMessage(ChatColor.GREEN + "用法: /coin " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            return true;
        }
        return false;
    }
}

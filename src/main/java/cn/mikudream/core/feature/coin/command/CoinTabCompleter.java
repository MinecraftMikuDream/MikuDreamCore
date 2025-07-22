package cn.mikudream.core.feature.coin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoinTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("add", "list", "version", "remove", "reload");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if ("add".equals(subCommand)) {
                return null;
            }

            if ("list".equals(subCommand)) {
                return null;
            }

            if ("remove".equals(subCommand)) {
                return null;
            }

            if ("reload".equals(subCommand)) {
                return null;
            }
        }

        return completions;
    }
}
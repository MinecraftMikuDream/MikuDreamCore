package cn.mikudream.core.feature.friend.command;

import cn.mikudream.core.feature.friend.Friend;
import cn.mikudream.core.feature.friend.FriendSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FriendTabCompleter implements TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list", "accept");
    private final FriendSystem friendSystem;

    public FriendTabCompleter(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

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

        if (args.length == 2 && sender instanceof Player) {
            Player player = (Player) sender;
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "add":
                    // 建议所有玩家（排除自己和已有好友）
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !p.equals(player))
                            .filter(p -> !friendSystem.getFriends(player.getUniqueId()).contains(p.getUniqueId()))
                            .map(Player::getName)
                            .forEach(completions::add);
                    break;
                case "remove":
                case "accept":
                    // 建议待处理请求或现有好友
                    if ("accept".equals(subCommand)) {
                        friendSystem.getPendingRequests(player.getUniqueId()).stream()
                                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                .forEach(completions::add);
                    } else {
                        friendSystem.getFriends(player.getUniqueId()).stream()
                                .map(Friend::getFriendName)
                                .forEach(completions::add);
                    }
                    break;
                case "list":
                    friendSystem.getFriends(player.getUniqueId()).stream()
                            .map(Friend::getFriendName)
                            .forEach(completions::add);
                    break;
                case "deny", "tp":
                    friendSystem.getPendingRequests(player.getUniqueId()).stream()
                            .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                            .forEach(completions::add);
                    break;
            }
        }

        return completions;
    }
}
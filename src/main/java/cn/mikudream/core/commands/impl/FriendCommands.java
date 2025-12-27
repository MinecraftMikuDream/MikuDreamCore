package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.SubCommand;
import cn.mikudream.core.feature.friend.FriendSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 好友系统命令
 */
public class FriendCommands extends SubCommand {
    private final FriendSystem friendSystem;

    public FriendCommands(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    @Override
    public String getName() {
        return "friend";
    }

    @Override
    public String getDescriptionText() {
        return "好友系统命令";
    }

    @Override
    public String getUsage() {
        return "/friend <add|remove|list|accept|deny|tp> [玩家]";
    }

    @Override
    protected void registerSubCommands() {
        // 添加好友
        registerSubCommand("add", (player, args) -> {
            if (args.length < 1) {
                player.sendMessage("§c用法: /friend add <玩家>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§c玩家不存在或不在线!");
                return true;
            }

            if (player.equals(target)) {
                player.sendMessage("§c不能添加自己为好友!");
                return true;
            }

            friendSystem.addFriendRequest(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§a已向 " + target.getName() + " 发送好友请求!");

            if (target.isOnline()) {
                target.sendMessage("§e" + player.getName() + " 想添加你为好友!");
                target.sendMessage("§e使用 /friend accept " + player.getName() + " 接受请求");
            }

            return true;
        });

        // 删除好友
        registerSubCommand("remove", (player, args) -> {
            if (args.length < 1) {
                player.sendMessage("§c用法: /friend remove <玩家>");
                return true;
            }

            var target = Bukkit.getOfflinePlayer(args[0]);
            boolean removed = friendSystem.removeFriend(player.getUniqueId(), target.getUniqueId());

            if (removed) {
                player.sendMessage("§a已删除好友: " + target.getName());
            } else {
                player.sendMessage("§c该玩家不在你的好友列表中!");
            }

            return true;
        });

        // 列出好友
        registerSubCommand("list", (player, args) -> {
            var friends = friendSystem.getFriends(player.getUniqueId());

            if (friends.isEmpty()) {
                player.sendMessage("§e你还没有好友!");
                return true;
            }

            player.sendMessage("§6§l好友列表 (§e" + friends.size() + "§6)");
            friends.forEach(friend -> {
                var offlinePlayer = Bukkit.getOfflinePlayer(friend.getFriendId());
                String status = offlinePlayer.isOnline() ? "§a在线" : "§7离线";
                player.sendMessage(" §7- " + offlinePlayer.getName() + " " + status);
            });

            return true;
        });

        // 接受好友请求
        registerSubCommand("accept", (player, args) -> {
            if (args.length < 1) {
                player.sendMessage("§c用法: /friend accept <玩家>");
                return true;
            }

            var target = Bukkit.getOfflinePlayer(args[0]);
            boolean accepted = friendSystem.acceptFriendRequest(player.getUniqueId(), target.getUniqueId());

            if (accepted) {
                player.sendMessage("§a已接受 " + target.getName() + " 的好友请求!");
                if (target.isOnline()) {
                    Objects.requireNonNull(target.getPlayer()).sendMessage("§e" + player.getName() + " 接受了你的好友请求!");
                }
            } else {
                player.sendMessage("§c没有来自该玩家的好友请求!");
            }

            return true;
        });

        // 拒绝好友请求
        registerSubCommand("deny", (player, args) -> {
            if (args.length < 1) {
                player.sendMessage("§c用法: /friend deny <玩家>");
                return true;
            }

            var target = Bukkit.getOfflinePlayer(args[0]);
            boolean denied = friendSystem.denyFriendRequest(player.getUniqueId(), target.getUniqueId());

            if (denied) {
                player.sendMessage("§a已拒绝 " + target.getName() + " 的好友请求!");
                if (target.isOnline()) {
                    Objects.requireNonNull(target.getPlayer()).sendMessage("§c" + player.getName() + " 拒绝了你的好友请求!");
                }
            } else {
                player.sendMessage("§c没有来自该玩家的好友请求!");
            }

            return true;
        });

        // 传送至好友
        registerSubCommand("tp", (player, args) -> {
            if (args.length < 1) {
                player.sendMessage("§c用法: /friend tp <玩家>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§c玩家不在线!");
                return true;
            }

            // 检查是否是好友
            boolean isFriend = friendSystem.getFriends(player.getUniqueId()).stream()
                    .anyMatch(friend -> friend.getFriendId().equals(target.getUniqueId()));

            if (!isFriend) {
                player.sendMessage("§c只能传送到好友!");
                return true;
            }

            player.teleport(target);
            player.sendMessage("§a已传送至好友: " + target.getName());

            return true;
        });

        // 设置默认处理器为显示帮助
        registerDefaultHandler((player, args) -> showHelp(player));
    }

    @Override
    protected Optional<List<String>> tabCompletePlayer(Player player, org.bukkit.command.Command command,
                                                       String label, String[] args) {
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            List<String> suggestions = switch (subCommand) {
                case "add" -> getOnlinePlayersExcept(player);
                case "remove", "tp" -> getFriendNames(player);
                case "accept", "deny" -> getPendingRequestNames(player);
                default -> List.of();
            };

            return Optional.of(suggestions.stream()
                    .filter(name -> name.startsWith(args[1]))
                    .collect(Collectors.toList()));
        }

        return super.tabCompletePlayer(player, command, label, args);
    }

    private List<String> getOnlinePlayersExcept(Player player) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private List<String> getFriendNames(Player player) {
        return friendSystem.getFriends(player.getUniqueId()).stream()
                .map(friend -> Bukkit.getOfflinePlayer(friend.getFriendId()).getName())
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> getPendingRequestNames(Player player) {
        return friendSystem.getPendingRequests(player.getUniqueId()).stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());
    }
}
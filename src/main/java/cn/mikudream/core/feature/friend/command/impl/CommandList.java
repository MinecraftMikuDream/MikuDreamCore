package cn.mikudream.core.feature.friend.command.impl;

import cn.mikudream.core.feature.friend.Friend;
import cn.mikudream.core.feature.friend.FriendSystem;
import cn.mikudream.core.feature.friend.command.FriendSubCommand;
import cn.mikudream.core.feature.friend.command.FriendCommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@FriendCommandInfo(
        name = "list",
        purpose = "列出好友",
        syntax = "[页码]"
)
public class CommandList extends FriendSubCommand {
    private final FriendSystem friendSystem;

    public CommandList(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    @Override
    protected boolean execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令!");
            return true;
        }

        Player player = (Player) sender;
        List<Friend> friends = friendSystem.getFriends(player.getUniqueId());

        if (friends.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "你还没有好友!");
            return true;
        }

        printSeparator(player);
        player.sendMessage(ChatColor.AQUA + "你的好友列表 (" + friends.size() + "):");

        for (Friend friend : friends) {
            String status = Bukkit.getPlayer(friend.getFriendId()) != null
                    ? ChatColor.GREEN + "在线"
                    : ChatColor.GRAY + "离线";

            player.sendMessage(ChatColor.GOLD + " - " + friend + " " + status);
        }

        printSeparator(player);
        return true;
    }
}
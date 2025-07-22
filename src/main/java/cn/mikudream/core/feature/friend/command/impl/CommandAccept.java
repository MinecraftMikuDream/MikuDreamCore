package cn.mikudream.core.feature.friend.command.impl;

import cn.mikudream.core.feature.friend.FriendSystem;
import cn.mikudream.core.feature.friend.command.FriendSubCommand;
import cn.mikudream.core.feature.friend.command.FriendCommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@FriendCommandInfo(
        name = "accept",
        purpose = "接受好友请求",
        syntax = "<玩家名>"
)
public class CommandAccept extends FriendSubCommand {
    private final FriendSystem friendSystem;

    public CommandAccept(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    @Override
    protected boolean execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令!");
            return true;
        }

        if (args.length < 2) {
            return false; // 显示语法
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        boolean accepted = friendSystem.acceptFriendRequest(player.getUniqueId(), target.getUniqueId());

        if (accepted) {
            player.sendMessage(ChatColor.GREEN + "你已接受 " + targetName + " 的好友请求!");
            if (target.isOnline()) {
                target.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " 接受了你的好友请求!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "没有来自 " + targetName + " 的好友请求!");
        }

        return true;
    }
}
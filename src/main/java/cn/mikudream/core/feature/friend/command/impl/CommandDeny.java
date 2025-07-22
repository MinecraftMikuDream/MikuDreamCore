package cn.mikudream.core.feature.friend.command.impl;

import cn.mikudream.core.feature.friend.FriendSystem;
import cn.mikudream.core.feature.friend.command.FriendCommandInfo;
import cn.mikudream.core.feature.friend.command.FriendSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@FriendCommandInfo(
        name = "deny",
        purpose = "拒绝好友请求",
        syntax = "/friend deny <好友名称>"
)
public class CommandDeny extends FriendSubCommand {
    private final FriendSystem friendSystem;

    public CommandDeny(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令!");
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        boolean removed = friendSystem.removeFriend(player.getUniqueId(), target.getUniqueId());

        if (removed) {
            player.sendMessage(ChatColor.GREEN + "已拒绝好友请求!");
            if (target.isOnline()) {
                target.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " 拒绝了你的好友请求!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "他没有向 " + targetName + " 发送好友请求!");
        }

        return true;
    }
}

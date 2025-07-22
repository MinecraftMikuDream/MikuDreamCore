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
        name = "tp",
        purpose = "传送好友",
        syntax = "/friend tp <好友名称>"
)
public class CommandTp extends FriendSubCommand {
    private final FriendSystem friendSystem;

    public CommandTp(FriendSystem friendSystem) {
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
            player.sendMessage(ChatColor.GREEN + "已删除好友: " + targetName);
        } else {
            player.sendMessage(ChatColor.RED + targetName + " 不在你的好友列表中!");
        }

        return true;
    }
}

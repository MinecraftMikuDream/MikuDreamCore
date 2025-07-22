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
        name = "remove",
        purpose = "删除好友",
        syntax = "<玩家名>"
)
public class CommandRemove extends FriendSubCommand {
    private final FriendSystem friendSystem;

    public CommandRemove(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    @Override
    protected boolean execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令!");
            return true;
        }

        if (args.length < 2) {
            return false; // 显示语法
        }

        Player player = (Player) sender;
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
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
        name = "add",
        purpose = "添加好友",
        syntax = "<玩家名>"
)
public class CommandAdd extends FriendSubCommand {
    private final FriendSystem friendSystem;

    public CommandAdd(FriendSystem friendSystem) {
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

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "你不能添加自己为好友!");
            return true;
        }

        friendSystem.addFriendRequest(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "已向 " + targetName + " 发送好友请求!");

        if (target.isOnline()) {
            target.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " 想添加你为好友! 使用 /friend accept " + player.getName());
        }

        return true;
    }
}
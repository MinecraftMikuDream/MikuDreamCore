package cn.mikudream.core.feature.scoin.command.impl;

import cn.mikudream.core.feature.scoin.command.CommandInfo;
import cn.mikudream.core.feature.scoin.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name="version", purpose="Get SCoin Version")
public class CommandVersion extends SubCommand {

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(ChatColor.GREEN + "SCoin SubPlugin Version: 0.0.1-beta");
            player.sendMessage(ChatColor.GREEN + "MikuDream Version: 1.1.025");
            return true;
        }
        return false;
    }
}

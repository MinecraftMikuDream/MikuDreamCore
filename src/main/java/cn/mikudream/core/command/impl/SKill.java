package cn.mikudream.core.command.impl;

import cn.mikudream.core.MikuDream;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SKill implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Player player = (Player)((Object)sender);
        try {
             if (!MikuDream.skill_Enabled) {
                 player.sendMessage("§c当前自杀功能已被管理员关闭喵！");
                 return false;
             }
             // 玩家自杀
             player.setHealth(0.0);
             player.sendMessage("§6你已成功自杀喵");
             Bukkit.broadcastMessage("§e玩家 " + player.getName() + " 选择了自我了断喵，可惜捏");
         }
         catch (NullPointerException e) {
             Bukkit.broadcastMessage("§c出现了一个错误: " + e.getMessage());
         }
        return false;
    }
}

package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.PlayerCommand;
import cn.mikudream.core.MikuDream;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * 自杀命令
 */
public class SkillCommand extends PlayerCommand {

    public SkillCommand() {}

    @Override
    public String getName() {
        return "skill";
    }

    @Override
    public String getDescriptionText() {
        return "自杀命令";
    }

    @Override
    protected boolean executePlayer(Player player, Command command, String label, String[] args) {
        if (!MikuDream.skill_Enabled) {
            player.sendMessage("§c自杀功能已被管理员关闭!");
            return true;
        }

        player.setHealth(0.0);
        player.sendMessage("§6你已成功自杀!");
        Bukkit.broadcastMessage("§e玩家 " + player.getName() + " 选择了自我了断!");

        return true;
    }

    @Override
    protected Optional<List<String>> tabCompletePlayer(Player player, Command command, String label, String[] args) {
        return Optional.empty();
    }
}
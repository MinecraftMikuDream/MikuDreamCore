package cn.onea.sunkplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SRaffle implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Random rand = new Random();
        int min = 1;
        int max = 10;
        int randomNum = rand.nextInt(max - min + 1) + min;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return false;
        }
        if(randomNum == 1)
        {
            String command_1 = "money + " + sender.getName() + " 1000";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command_1);
            Bukkit.broadcastMessage("§e玩家 " + sender.getName() + " 得到了1000元!");
        }
        else if(randomNum == 2)
        {
            String command_1 = "msg " + sender.getName() + " 没中奖！";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command_1);
        }
        else{
            String command_1 = "msg " + sender.getName() + " 再来一次";
            String command_2 = "raffle " + sender.getName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command_1);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command_2);
        }
        return false;
    }
}

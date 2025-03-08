package cn.onea.sunkplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SRaffle implements CommandExecutor {
    private static final Map<UUID, Integer> counterMap = new HashMap<>(); // 保底计数器
    private static final Map<UUID, Long> lastPlayMap = new HashMap<>();  // 最后抽奖时间
    private static final int PITY_THRESHOLD = 99; // 保底阈值（第100次必中）
    private static final String ADMIN_PERMISSION = "sunk.raffle.admin";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以使用这个命令喵！");
            return true;
        }

        UUID uuid = player.getUniqueId();
        boolean isAdmin = player.hasPermission(ADMIN_PERMISSION);

        // ----------------------------
        // 每日限制逻辑（管理员豁免）
        // ----------------------------
        if (!isAdmin) {
            long lastPlayTime = lastPlayMap.getOrDefault(uuid, 0L);
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTimeInMillis(lastPlayTime);
            Calendar nowCal = Calendar.getInstance();

            // 检查是否是同一天
            if (lastCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                    lastCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) {
                player.sendMessage("§c今天已经抽过奖了，明天再来吧喵~");
                return true;
            }
            // 更新最后抽奖时间
            lastPlayMap.put(uuid, System.currentTimeMillis());
        }

        // ----------------------------
        // 保底逻辑
        // ----------------------------
        int counter = counterMap.getOrDefault(uuid, 0);
        boolean forceWin = (counter >= PITY_THRESHOLD);

        // 生成随机结果（1~10）
        Random rand = new Random();
        int randomNum = forceWin ? 1 : rand.nextInt(10) + 1;

        // ----------------------------
        // 处理结果
        // ----------------------------
        if (randomNum == 1 || forceWin) {
            // 中奖逻辑
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " 1000");
            Bukkit.broadcastMessage("§e玩家 " + player.getName() + " 得到了1000元!");
            counterMap.put(uuid, 0); // 重置保底计数器
        } else if (randomNum == 2) {
            // 未中奖
            player.sendMessage("§6没中奖！");
            if (!isAdmin) counterMap.put(uuid, counter + 1); // 管理员不计数
        } else {
            // 递归调用（但不会触发每日限制）
            player.sendMessage("§a再来一次~");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sraffle " + player.getName());
        }

        return true;
    }
}
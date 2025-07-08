package cn.mikudream.core.feature.breakboard;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.stream.Collectors;

public class BreakBoardManager {
    private final Map<UUID, Map<String, Integer>> playerData = new HashMap<>();
    private final Set<UUID> enabledPlayers = new HashSet<>();

    public boolean isBoardEnabled(UUID uuid) {
        return enabledPlayers.contains(uuid);
    }

    // 检查工具是否为镐或斧头
    public boolean isValidTool(ItemStack tool) {
        if (tool == null) return false;
        Material type = tool.getType();
        return type.name().endsWith("_PICKAXE") || type.name().endsWith("_AXE");
    }

    // 更新玩家挖掘次数
    public void updateBreakCount(Player player, ItemStack tool) {
        UUID uuid = player.getUniqueId();
        String toolType = tool.getType().name();

        playerData.putIfAbsent(uuid, new HashMap<>());
        Map<String, Integer> tools = playerData.get(uuid);
        tools.put(toolType, tools.getOrDefault(toolType, 0) + 1);
    }

    // 切换计分板显示状态
    public void toggleBoard(Player player) {
        UUID uuid = player.getUniqueId();
        if (enabledPlayers.contains(uuid)) {
            enabledPlayers.remove(uuid);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        } else {
            enabledPlayers.add(uuid);
            updateScoreboard(player);
        }
    }

    // 更新计分板内容
    void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("breakboard", "dummy", "§6§l挖掘量");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 按总挖掘次数排序
        List<Map.Entry<UUID, Integer>> sorted = getSortedData();
        for (int i = 0; i < Math.min(15, sorted.size()); i++) {
            Map.Entry<UUID, Integer> entry = sorted.get(i);
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            obj.getScore("§e" + name).setScore(entry.getValue());
        }

        player.setScoreboard(board);
    }

    // 获取排序后的数据
    private List<Map.Entry<UUID, Integer>> getSortedData() {
        Map<UUID, Integer> totalCounts = new HashMap<>();
        for (Map.Entry<UUID, Map<String, Integer>> entry : playerData.entrySet()) {
            int total = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            totalCounts.put(entry.getKey(), total);
        }
        return totalCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
    }
}
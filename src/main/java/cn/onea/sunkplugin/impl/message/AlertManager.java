package cn.onea.sunkplugin.impl.message;

import cn.onea.sunkplugin.SunkPlugins;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager {
    private final SunkPlugins plugin;
    private final Map<UUID, Long> lastAlertTime = new ConcurrentHashMap<>();

    public AlertManager(SunkPlugins plugin) {
        this.plugin = plugin;
    }

    public void logAction(Player player, String reason) {
        // 控制台记录
        if (plugin.getConfig().getBoolean("logging.console", true)) {
            String logMsg = String.format("[%s] %s 触发防护 - 原因: %s",
                    plugin.getName(),
                    player.getName(),
                    reason);

            switch (plugin.getConfig().getString("logging.level", "WARNING")) {
                case "DEBUG" -> plugin.getLogger().fine(logMsg);
                case "INFO" -> plugin.getLogger().info(logMsg);
                default -> plugin.getLogger().warning(logMsg);
            }
        }

        // 游戏内通知
        if (plugin.getConfig().getBoolean("alerts.in-game", true)) {
            long now = System.currentTimeMillis();
            long last = lastAlertTime.getOrDefault(player.getUniqueId(), 0L);

            if (now - last > plugin.getConfig().getLong("alerts.cooldown", 2000L)) {
                String alertMsg = plugin.getConfig().getString("messages.alert",
                        "§c[pcloy] 检测到可疑操作: %player%");

                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Player admin : Bukkit.getOnlinePlayers()) {
                        if (admin.hasPermission(Objects.requireNonNull(plugin.getConfig().getString("alerts.permission")))) {
                            admin.sendMessage(alertMsg.replace("%player%", player.getName()));
                        }
                    }
                });

                lastAlertTime.put(player.getUniqueId(), now);
            }
        }
    }
}
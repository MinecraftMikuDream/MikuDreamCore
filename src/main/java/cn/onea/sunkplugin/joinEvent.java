package cn.onea.sunkplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class joinEvent
        implements Listener {
    @EventHandler
    public void JoinHidePlayer(PlayerJoinEvent event) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            Player player;
            if (!otherPlayer.getDisplayName().contains("隐身") || (player = otherPlayer.getPlayer()) == null) continue;
            event.getPlayer().hidePlayer(player);
        }
    }
}

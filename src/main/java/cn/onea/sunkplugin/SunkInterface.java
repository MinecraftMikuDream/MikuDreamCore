package cn.onea.sunkplugin;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface SunkInterface extends Plugin, Listener {
    String ADMIN_PERMISSION = "cat.admin";

    void onEnable(@NotNull CommandSender sender);

    @Override
    void onDisable();

    @EventHandler
    void onPlayerChangedWorld(PlayerChangedWorldEvent event);

    void updatehome_xyz(int x, int y, int z);

    void setcatkill(boolean flag, CommandSender sender);
}

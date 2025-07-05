package cn.mikudream.core;

import cn.mikudream.core.BreakBoard.BreakBoardManager;
import cn.mikudream.core.BreakBoard.BreakListener;
import cn.mikudream.core.command.BreakBoardCommand;
import cn.mikudream.core.command.Hub;
import cn.mikudream.core.command.SConfigCommand;
import cn.mikudream.core.command.SKill;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MikuDream extends JavaPlugin implements Listener {
    public static boolean skill_Enabled = true;
    public int lobby_x = getConfig().getInt("lobby.x");
    public int lobby_y = getConfig().getInt("lobby.y");
    public int lobby_z = getConfig().getInt("lobby.z");
    public String lobby_world = getConfig().getString("lobby.dimension");

    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        skill_Enabled = this.getConfig().getBoolean("Skill", true);
        // 注册命令
        BreakBoardManager breakBoardManager = new BreakBoardManager();
        this.getServer().getPluginManager().registerEvents(new BreakListener(breakBoardManager), this);
        try {
            this.getCommand("sbreakboard").setExecutor(new BreakBoardCommand(breakBoardManager));
            this.getCommand("skill").setExecutor(new SKill());
            this.getCommand("sconfig").setExecutor(new SConfigCommand(this));
            this.getCommand("hub").setExecutor(new Hub(this));
        } catch (Exception e) {
            Bukkit bukkit;
            Bukkit.getLogger().severe("注册命令时出现错误: " + e.getMessage());
        }

    }

    @Override
    public void onDisable() {
        this.getConfig().set("kill_enabled", skill_Enabled);
        this.saveConfig();
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (event.getFrom().getName().equalsIgnoreCase(lobby_world)) {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage("§e你已离开主城，游戏模式已切换为生存");
            }
        }
    }

    public void updatelobby_xyz(int x, int y, int z) {
        getConfig().set("lobby.x", x);
        getConfig().set("lobby.y", y);
        getConfig().set("lobby.z", z);
        lobby_x = getConfig().getInt("lobby.x");
        lobby_y = getConfig().getInt("lobby.y");
        lobby_z = getConfig().getInt("lobby.z");
        saveConfig();
    }

    public void setcatkill(boolean flag, CommandSender sender) {
        getConfig().set("kill_enabled", flag);
        saveConfig();
        sender.sendMessage("§a已将 skill 功能设置为: " + skill_Enabled);
        getLogger().info("skill 功能设置为: " + skill_Enabled);
    }
}

package cn.mikudream.core;

import cn.mikudream.core.command.SCommand;
import cn.mikudream.core.feature.scoin.SCoinManager;
import cn.mikudream.core.feature.scoin.SCoinTabCompleter;
import cn.mikudream.core.feature.scoin.command.SCoinCommandExecutor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
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

    public static MikuDream getInstance() {
        return getPlugin(MikuDream.class);
    }

    public void onEnable() {
        // config
        saveDefaultConfig();
        reloadConfig();
        skill_Enabled = this.getConfig().getBoolean("Skill", true);

        // command
        SCommand scommand = new SCommand();
        scommand.init();

        SCoinManager sCoinManager = new SCoinManager(this);
        SCoinCommandExecutor command = new SCoinCommandExecutor(sCoinManager);
        PluginCommand scoinCommand = this.getCommand("scoin");
        if (scoinCommand != null) {
            scoinCommand.setExecutor(command);
            scoinCommand.setTabCompleter(new SCoinTabCompleter());
        } else {
            getLogger().severe("无法注册 scoin 命令，请检查 plugin.yml 配置");
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

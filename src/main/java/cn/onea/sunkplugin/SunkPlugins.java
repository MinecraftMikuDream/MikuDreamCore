package cn.onea.sunkplugin;

import cn.onea.sunkplugin.BreakBoard.BreakBoardManager;
import cn.onea.sunkplugin.BreakBoard.BreakListener;
import cn.onea.sunkplugin.command.*;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SunkPlugins extends JavaPlugin{
    public static boolean featureEnabled = true;
    public int homex = getConfig().getInt("home.x");
    public int homey = getConfig().getInt("home.y");
    public int homez = getConfig().getInt("home.z");
    CoinManager coinManager = new CoinManager(this);

    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        featureEnabled = this.getConfig().getBoolean("kill_enabled", true);
        // 注册命令
        BreakBoardManager breakBoardManager = new BreakBoardManager();
        this.getServer().getPluginManager().registerEvents(new BreakListener(breakBoardManager), this);
        this.getCommand("sbreakboard").setExecutor(new BreakBoardCommand(breakBoardManager));
        this.getCommand("sunkcoin").setExecutor(new CoinCommand(coinManager));
        this.getCommand("sc").setExecutor(new CoinCommand(coinManager));
        this.getCommand("skill").setExecutor(new SKill());
        this.getCommand("sconfig").setExecutor(new SConfigCommand(this));
        this.getCommand("sraffle").setExecutor(new SRaffle());
        this.getCommand("hub").setExecutor(new Hub(this));
        this.getCommand("vanish").setExecutor(new vanish(this));
        this.getCommand("v").setExecutor(new vanish(this));

        this.getServer().getPluginManager().registerEvents(new joinEvent(), this);
    }

    @Override
    public void onDisable() {
        coinManager.saveData();
        this.getConfig().set("kill_enabled", featureEnabled);
        this.saveConfig();
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (event.getFrom().getName().equalsIgnoreCase("the_void")) {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage("§e你已离开主城，游戏模式已切换为生存");
            }
        }
    }

    public void updatehome_xyz(int x, int y, int z) {
        getConfig().set("home.x", x);
        getConfig().set("home.y", y);
        getConfig().set("home.z", z);
        homex = getConfig().getInt("home.x");
        homey = getConfig().getInt("home.y");
        homez = getConfig().getInt("home.z");
        saveConfig();
    }

    public void setcatkill(boolean flag, CommandSender sender) {
        getConfig().set("kill_enabled", flag);
        saveConfig();
        sender.sendMessage("§a已将 catkill 功能设置为: " + featureEnabled);
        getLogger().info("catkill 功能设置为: " + featureEnabled);
    }
}

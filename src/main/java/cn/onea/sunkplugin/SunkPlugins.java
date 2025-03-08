package cn.onea.sunkplugin;

import cn.onea.sunkplugin.anitcheat.impl.Protocol.PacketListener;
import cn.onea.sunkplugin.command.Hub;
import cn.onea.sunkplugin.command.SConfigCommand;
import cn.onea.sunkplugin.command.SKill;
import cn.onea.sunkplugin.command.SRaffle;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SunkPlugins extends JavaPlugin implements SunkInterface {
    private final Cache<UUID, PlayerData> playerData = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    public static boolean featureEnabled = true;
    private static final String CONFIG_KEY = "kill_enabled";
    public int homex = getConfig().getInt("home.x");
    public int homey = getConfig().getInt("home.y");
    public int homez = getConfig().getInt("home.z");

    @Override
    public void onEnable(@NotNull CommandSender sender) {
        InputStream defConfigStream = this.getResource("config.yml");
        if (defConfigStream != null) {
            this.reloadConfig();
        }
        else {
            this.getLogger().warning("无法加载默认配置文件！");
            this.saveDefaultConfig();
        }

        PacketListener packetListener = new PacketListener();
        packetListener.register();

        featureEnabled = this.getConfig().getBoolean(CONFIG_KEY, true);
        // 检查是否存在 home 节点
        if (!getConfig().contains("home")) {
            // 如果没有则设置默认值
            getConfig().set("home.x", 0);
            getConfig().set("home.y", 0);
            getConfig().set("home.z", 0);
            saveConfig();
            getLogger().info("已创建 home 配置项");
        } else {
            getLogger().info("home 配置项已存在，跳过创建");
        }

        // 注册监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("skill").setExecutor(new SKill());
        getCommand("sconfig").setExecutor(new SConfigCommand());
        getCommand("sraffle").setExecutor(new SRaffle());
        getCommand("hub").setExecutor(new Hub());
    }

    @Override
    public void onDisable() {
        this.getConfig().set(CONFIG_KEY, featureEnabled);
        this.saveConfig();
    }

    @Override
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

    @Override
    public void updatehome_xyz(int x, int y, int z) {
        getConfig().set("home.x", x);
        getConfig().set("home.y", y);
        getConfig().set("home.z", z);
        homex = getConfig().getInt("home.x");
        homey = getConfig().getInt("home.y");
        homez = getConfig().getInt("home.z");
        saveConfig();
    }

    @Override
    public void setcatkill(boolean flag, CommandSender sender) {
        getConfig().set(CONFIG_KEY, flag);
        saveConfig();
        sender.sendMessage("§a已将 catkill 功能设置为: " + featureEnabled);
        getLogger().info("catkill 功能设置为: " + featureEnabled);
    }
}

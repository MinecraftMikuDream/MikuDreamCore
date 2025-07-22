package cn.mikudream.core;

import cn.mikudream.core.command.SCommand;
import cn.mikudream.core.command.impl.SVCommand;
import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.coin.command.CoinTabCompleter;
import cn.mikudream.core.feature.coin.command.CoinCommandExecutor;
import cn.mikudream.core.feature.friend.FriendSystem;
import cn.mikudream.core.feature.friend.command.FriendCommandExecutor;
import cn.mikudream.core.feature.friend.command.FriendCommandInfo;
import cn.mikudream.core.feature.friend.command.FriendTabCompleter;
import cn.mikudream.core.feature.shop.command.ShopCommandExecutor;
import cn.mikudream.core.feature.shop.command.ShopTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class MikuDream extends JavaPlugin implements Listener {
    public static boolean skill_Enabled = true;
    public int lobby_x = getConfig().getInt("lobby.x");
    public int lobby_y = getConfig().getInt("lobby.y");
    public int lobby_z = getConfig().getInt("lobby.z");
    public String lobby_world = getConfig().getString("lobby.dimension");
    private FriendSystem friendSystem;

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

        CoinsManager mikuCoinsManager = new CoinsManager(this);
        CoinCommandExecutor command = new CoinCommandExecutor(mikuCoinsManager);
        PluginCommand scoinCommand = this.getCommand("coin");

        ShopCommandExecutor shopCommandExecutor = new ShopCommandExecutor();
        PluginCommand shopCommand = this.getCommand("shop");

        FriendCommandExecutor friendCommandExecutor = new FriendCommandExecutor(friendSystem);
        PluginCommand friendCommand = this.getCommand("friend");

        if (scoinCommand != null && shopCommand != null && friendCommand != null) {
            scoinCommand.setExecutor(command);
            scoinCommand.setTabCompleter(new CoinTabCompleter());

            shopCommand.setExecutor(shopCommandExecutor);
            shopCommand.setTabCompleter(new ShopTabCompleter());

            friendCommand.setExecutor(friendCommandExecutor);
            friendCommand.setTabCompleter(new FriendTabCompleter(friendSystem));

        } else {
            getLogger().severe("无法注册 coin,shop 命令，请检查 plugin.yml 配置");
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

    public void setskill(boolean flag, CommandSender sender) {
        getConfig().set("kill_enabled", flag);
        saveConfig();
        sender.sendMessage("§a已将 skill 功能设置为: " + skill_Enabled);
        getLogger().info("skill 功能设置为: " + skill_Enabled);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        for (UUID uuid : SVCommand.invisiblePlayers) {
            Player invisiblePlayer = Bukkit.getPlayer(uuid);
            if (invisiblePlayer != null && !invisiblePlayer.equals(newPlayer)) {
                newPlayer.hidePlayer(this, invisiblePlayer);
            }
        }
    }
}

package cn.mikudream.core;

import cn.mikudream.core.commands.CommandRegistry;
import cn.mikudream.core.commands.impl.*;
import cn.mikudream.core.feature.protection.SpecialItemProtection;
import cn.mikudream.core.managers.ManagerFactory;
import cn.mikudream.core.feature.shop.listener.ShopListener;
import cn.mikudream.core.feature.playermarket.listener.PlayerMarketListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MikuDream extends JavaPlugin implements Listener {
    // 配置字段
    private static final String CONFIG_LOBBY_WORLD = "lobby.world";
    private static final String CONFIG_LOBBY_X = "lobby.x";
    private static final String CONFIG_LOBBY_Y = "lobby.y";
    private static final String CONFIG_LOBBY_Z = "lobby.z";
    private static final String CONFIG_SKILL_ENABLED = "skill.enabled";

    // 配置默认值
    private static final String DEFAULT_WORLD = "world";
    private static final int DEFAULT_X = 0;
    private static final int DEFAULT_Y = 64;
    private static final int DEFAULT_Z = 0;
    private static final boolean DEFAULT_SKILL_ENABLED = true;

    // 运行时配置
    public static boolean skill_Enabled;
    public int lobby_x;
    public int lobby_y;
    public int lobby_z;
    public String lobby_world;

    // 管理器
    private ManagerFactory managerFactory;
    private CommandRegistry commandRegistry;
    private SpecialItemProtection specialItemProtection;

    // 隐身玩家集合
    private final Set<UUID> invisiblePlayers = ConcurrentHashMap.newKeySet();

    public static MikuDream getInstance() {
        return getPlugin(MikuDream.class);
    }

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        reloadConfig();

        // 加载配置
        loadConfig();

        // 初始化管理器工厂
        this.managerFactory = new ManagerFactory(this);
        try {
            managerFactory.initializeAll();
            getLogger().info("管理器工厂初始化完成");
        } catch (ManagerFactory.ManagerInitializationException e) {
            getLogger().severe("管理器初始化失败: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化命令注册表
        this.commandRegistry = new CommandRegistry(this);

        // 注册依赖
        registerDependencies();

        // 注册命令
        registerCommands();

        // 注册事件监听器
        registerListeners();

        // 注册自身作为事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        specialItemProtection = new SpecialItemProtection(this);
        Bukkit.getPluginManager().registerEvents(specialItemProtection, this);

        getLogger().info("MikuDream插件已启用! 版本: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // 保存配置
        saveConfig();

        // 关闭管理器
        if (managerFactory != null) {
            managerFactory.shutdown();
        }

        // 清理资源
        invisiblePlayers.clear();
        specialItemProtection.cleanupAllEffects();

        getLogger().info("MikuDream插件已禁用");
    }

    private void loadConfig() {
        // 加载大厅坐标
        this.lobby_world = getConfig().getString(CONFIG_LOBBY_WORLD, DEFAULT_WORLD);
        this.lobby_x = getConfig().getInt(CONFIG_LOBBY_X, DEFAULT_X);
        this.lobby_y = getConfig().getInt(CONFIG_LOBBY_Y, DEFAULT_Y);
        this.lobby_z = getConfig().getInt(CONFIG_LOBBY_Z, DEFAULT_Z);

        // 加载自杀功能开关
        skill_Enabled = getConfig().getBoolean(CONFIG_SKILL_ENABLED, DEFAULT_SKILL_ENABLED);

        if (!getConfig().contains(CONFIG_LOBBY_WORLD)) {
            getConfig().set(CONFIG_LOBBY_WORLD, DEFAULT_WORLD);
        }
        if (!getConfig().contains(CONFIG_LOBBY_X)) {
            getConfig().set(CONFIG_LOBBY_X, DEFAULT_X);
        }
        if (!getConfig().contains(CONFIG_LOBBY_Y)) {
            getConfig().set(CONFIG_LOBBY_Y, DEFAULT_Y);
        }
        if (!getConfig().contains(CONFIG_LOBBY_Z)) {
            getConfig().set(CONFIG_LOBBY_Z, DEFAULT_Z);
        }
        if (!getConfig().contains(CONFIG_SKILL_ENABLED)) {
            getConfig().set(CONFIG_SKILL_ENABLED, DEFAULT_SKILL_ENABLED);
        }

        saveConfig();
        getLogger().info("配置加载完成");
    }

    private void registerDependencies() {
        // 注册管理器依赖
        commandRegistry.registerDependency(
                cn.mikudream.core.managers.CoinsManager.class,
                managerFactory.getCoinsManager()
        );
        commandRegistry.registerDependency(
                cn.mikudream.core.feature.friend.FriendSystem.class,
                managerFactory.getFriendSystem()
        );
        commandRegistry.registerDependency(
                cn.mikudream.core.feature.lottery.LotterySystem.class,
                managerFactory.getLotterySystem()
        );
    }

    private void registerCommands() {
        // 注册所有命令
        commandRegistry.registerCommand("friend", new FriendCommands(
                managerFactory.getFriendSystem()
        ));

        commandRegistry.registerCommand("coin", new CoinCommands(
                managerFactory.getCoinsManager()
        ));

        commandRegistry.registerCommand("shop", new ShopCommand());

        commandRegistry.registerCommand("market", new MarketCommand());

        commandRegistry.registerCommand("lottery", new LotteryCommand(
                managerFactory.getLotterySystem()
        ));

        commandRegistry.registerCommand("hub", new HubCommand(this));
        commandRegistry.registerCommand("skill", new SkillCommand());
        commandRegistry.registerCommand("sv", new InvisibleCommand(this, invisiblePlayers));
        commandRegistry.registerCommand("catconfig", new ConfigCommands(this));

        getLogger().info("所有命令已注册");
    }

    private void registerListeners() {
        // 注册商店监听器
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);

        // 注册玩家市场监听器
        Bukkit.getPluginManager().registerEvents(new PlayerMarketListener(), this);

        getLogger().info("事件监听器注册完成");
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getName();

        // 如果从主城离开，切换到生存模式
        if (fromWorld.equalsIgnoreCase(lobby_world) &&
                player.getGameMode() == GameMode.ADVENTURE) {
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage("§e你已离开主城，游戏模式已切换为生存");
        }

        // 如果进入主城，切换到冒险模式
        if (player.getWorld().getName().equalsIgnoreCase(lobby_world) &&
                player.getGameMode() != GameMode.ADVENTURE) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage("§e你已进入主城，游戏模式已切换为冒险");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();

        // 处理隐身玩家
        for (UUID uuid : invisiblePlayers) {
            Player invisiblePlayer = Bukkit.getPlayer(uuid);
            if (invisiblePlayer != null && !invisiblePlayer.equals(newPlayer)) {
                newPlayer.hidePlayer(this, invisiblePlayer);
            }
        }

        // 如果玩家在主城，设置冒险模式
        if (newPlayer.getWorld().getName().equalsIgnoreCase(lobby_world)) {
            newPlayer.setGameMode(GameMode.ADVENTURE);
        }
    }

    // 配置更新方法
    public void updatelobby_xyz(int x, int y, int z) {
        getConfig().set(CONFIG_LOBBY_X, x);
        getConfig().set(CONFIG_LOBBY_Y, y);
        getConfig().set(CONFIG_LOBBY_Z, z);

        // 更新运行时值
        this.lobby_x = x;
        this.lobby_y = y;
        this.lobby_z = z;

        saveConfig();
        getLogger().info("主城坐标已更新: " + x + ", " + y + ", " + z);
    }

    public void setskill(boolean flag, CommandSender sender) {
        getConfig().set(CONFIG_SKILL_ENABLED, flag);
        skill_Enabled = flag;
        saveConfig();

        sender.sendMessage("§a自杀功能已" + (flag ? "启用" : "禁用"));
        getLogger().info("自杀功能已" + (flag ? "启用" : "禁用"));
    }

    // 隐身玩家管理方法
    public Set<UUID> getInvisiblePlayers() {
        return invisiblePlayers;
    }

    public boolean togglePlayerInvisibility(Player player) {
        UUID playerId = player.getUniqueId();

        if (invisiblePlayers.contains(playerId)) {
            // 解除隐身
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(this, player);
            }
            invisiblePlayers.remove(playerId);
            return false; // 不再隐身
        } else {
            // 进入隐身
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.hidePlayer(this, player);
                }
            }
            invisiblePlayers.add(playerId);
            return true; // 已隐身
        }
    }

    // 获取管理器
    public ManagerFactory getManagerFactory() {
        return managerFactory;
    }

    public cn.mikudream.core.managers.CoinsManager getCoinsManager() {
        return managerFactory.getCoinsManager();
    }

    public cn.mikudream.core.managers.ShopManager getShopManager() {
        return managerFactory.getShopManager();
    }

    public cn.mikudream.core.managers.PlayerMarketManager getPlayerMarketManager() {
        return managerFactory.getMarketManager();
    }

    public cn.mikudream.core.feature.friend.FriendSystem getFriendSystem() {
        return managerFactory.getFriendSystem();
    }

    public cn.mikudream.core.feature.lottery.LotterySystem getLotterySystem() {
        return managerFactory.getLotterySystem();
    }
}
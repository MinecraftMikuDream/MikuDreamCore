package cn.mikudream.core.managers;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.friend.FriendSystem;
import cn.mikudream.core.feature.lottery.LotterySystem;

import java.io.IOException;
import java.util.Objects;

public final class ManagerFactory {
    private final MikuDream plugin;
    private CoinsManager coinsManager;
    private ShopManager shopManager;
    private PlayerMarketManager marketManager;
    private FriendSystem friendSystem;
    private LotterySystem lotterySystem;

    public ManagerFactory(MikuDream plugin) {
        this.plugin = plugin;
    }

    public void initializeAll() throws ManagerInitializationException {
        try {
            this.coinsManager = new CoinsManager(plugin);
            this.shopManager = new ShopManager(plugin);
            this.marketManager = new PlayerMarketManager(plugin);
            this.friendSystem = new FriendSystem();
            this.lotterySystem = new LotterySystem(plugin, coinsManager);

            plugin.getLogger().info("所有管理器初始化完成");
        } catch (IOException e) {
            throw new ManagerInitializationException("管理器初始化失败", e);
        }
    }

    public CoinsManager getCoinsManager() {
        return Objects.requireNonNull(coinsManager, "CoinsManager未初始化");
    }

    public ShopManager getShopManager() {
        return Objects.requireNonNull(shopManager, "ShopManager未初始化");
    }

    public PlayerMarketManager getMarketManager() {
        return Objects.requireNonNull(marketManager, "PlayerMarketManager未初始化");
    }

    public FriendSystem getFriendSystem() {
        return Objects.requireNonNull(friendSystem, "FriendSystem未初始化");
    }

    public LotterySystem getLotterySystem() {
        return Objects.requireNonNull(lotterySystem, "LotterySystem未初始化");
    }

    public void shutdown() {
        getCoinsManager().shutdown();
    }

    public static class ManagerInitializationException extends Exception {
        public ManagerInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
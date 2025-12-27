package cn.mikudream.core.managers;

import cn.mikudream.core.MikuDream;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class CoinsManager extends BaseManager {
    private static CoinsManager instance;
    private final Map<UUID, Long> coinCache = new ConcurrentHashMap<>();
    private final YamlConfiguration data;
    private final Path dataFile;
    private final ScheduledExecutorService scheduler;
    private final String COIN_FILE = "coin.yml";

    CoinsManager(MikuDream plugin) throws IOException {
        super(plugin);
        this.dataFile = dataFolder.resolve(COIN_FILE);

        if (!Files.exists(dataFile)) {
            Files.createDirectories(dataFolder);
            plugin.saveResource(COIN_FILE, false);
        }

        this.data = YamlConfiguration.loadConfiguration(dataFile.toFile());
        this.scheduler = Executors.newScheduledThreadPool(1,
                Thread.ofVirtual().name("coins-saver-", 0).factory());

        loadToCache();
        startAutoSave();

        logger.info("CoinsManager initialized");
    }

    public static synchronized void init(MikuDream plugin) {
        if (instance == null) {
            try {
                instance = new CoinsManager(plugin);
            } catch (IOException e) {
                plugin.getLogger().severe("无法初始化CoinsManager: " + e.getMessage());
            }
        }
    }

    public @NotNull static CoinsManager getInstance() {
        return MikuDream.getInstance().getCoinsManager();
    }

    private void loadToCache() {
        data.getKeys(false).forEach(key -> {
            try {
                UUID uuid = UUID.fromString(key);
                long coins = data.getLong(key, 0);
                coinCache.put(uuid, coins);
            } catch (IllegalArgumentException e) {
                logger.warning("无效的UUID格式: " + key);
            }
        });
    }

    private void startAutoSave() {
        scheduler.scheduleAtFixedRate(this::saveData, 5, 5, TimeUnit.MINUTES);
    }

    public void saveData() {
        coinCache.forEach((uuid, coins) ->
                data.set(uuid.toString(), coins));

        try {
            saveYaml(data, COIN_FILE);
        } catch (IOException e) {
            logger.severe("无法保存coin.yml: " + e.getMessage());
        }
    }

    public long getCoins(UUID uuid) {
        return coinCache.getOrDefault(uuid, 0L);
    }

    public void setCoins(UUID uuid, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("金币数量不能为负数");
        }
        coinCache.put(uuid, amount);
        logger.info("设置金币: %s = %d".formatted(uuid, amount));
    }

    public void addCoins(UUID uuid, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("增加的金币数量必须为正数");
        }
        long current = getCoins(uuid);
        setCoins(uuid, current + amount);
    }

    public boolean removeCoins(UUID uuid, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("移除的金币数量必须为正数");
        }

        long current = getCoins(uuid);
        if (current >= amount) {
            setCoins(uuid, current - amount);
            return true;
        }

        logger.warning("金币不足: %s (当前: %d, 需要: %d)".formatted(uuid, current, amount));
        return false;
    }

    public boolean transferCoins(UUID from, UUID to, long amount) {
        if (removeCoins(from, amount)) {
            addCoins(to, amount);
            return true;
        }
        return false;
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        saveData();
    }
}
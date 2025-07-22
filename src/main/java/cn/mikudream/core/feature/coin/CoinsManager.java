package cn.mikudream.core.feature.coin;

import cn.mikudream.core.MikuDream;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CoinsManager {
    private final MikuDream plugin;
    private YamlConfiguration data;
    private File dataFile;

    public CoinsManager(MikuDream plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "coin.yml");

        if (!dataFile.exists()) {
            plugin.saveResource("coin.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getLogger().info("coin.yml loaded");
    }

    public void saveData() {
        try {
            data.save(dataFile);
            plugin.getLogger().info("coin.yml saved successfully");
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save coin.yml: " + e.getMessage());
        }
    }

    public int getCoins(UUID uuid) {
        loadData();
        String uuidStr = uuid.toString().toLowerCase();
        int coins = data.getInt(uuidStr, 0);
        plugin.getLogger().info("Query Coin: " + uuidStr + " -> " + coins);
        return coins;
    }

    public void setCoins(UUID uuid, long amount) {
        loadData();
        String uuidStr = uuid.toString().toLowerCase();
        data.set(uuidStr, amount);
        plugin.getLogger().info("set coin: " + uuidStr + " = " + amount);
        saveData();
    }

    public void addCoins(UUID uuid, long amount) {
        loadData();
        int current = getCoins(uuid);
        setCoins(uuid, current + amount);
        saveData();
    }

    public void removeCoins(UUID uuid, long amount) {
        loadData();
        int current = getCoins(uuid);
        if (getCoins(uuid) > 0) {
            setCoins(uuid, Math.max(0, current - amount));
            saveData();
        }
        else if(getCoins(uuid) == 0){
            plugin.getLogger().severe("Failed to remove coin: opponent's coin is 0");
        }
    }

    public void reload() {
        loadData();
        plugin.getLogger().info("coin.yml reloaded");
    }
}
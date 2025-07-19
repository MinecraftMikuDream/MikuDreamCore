package cn.mikudream.core.feature.scoin;

import cn.mikudream.core.MikuDream;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SCoinManager {
    private final MikuDream plugin;
    private YamlConfiguration data;
    private File dataFile;

    public SCoinManager(MikuDream plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "sunkcoins.yml");
        plugin.getLogger().info("加载金币文件: " + dataFile.getAbsolutePath());

        if (!dataFile.exists()) {
            plugin.saveResource("sunkcoins.yml", false);
            plugin.getLogger().info("创建新的金币文件");
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getLogger().info("金币文件加载完成");
    }

    public void saveData() {
        try {
            data.save(dataFile);
            plugin.getLogger().info("金币数据保存成功");
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存金币数据: " + e.getMessage());
        }
    }

    public int getCoins(UUID uuid) {
        loadData();
        String uuidStr = uuid.toString().toLowerCase();
        int coins = data.getInt(uuidStr, 0);
        plugin.getLogger().info("查询硬币: " + uuidStr + " -> " + coins);
        return coins;
    }

    public void setCoins(UUID uuid, int amount) {
        loadData();
        String uuidStr = uuid.toString().toLowerCase();
        data.set(uuidStr, amount);
        plugin.getLogger().info("设置硬币: " + uuidStr + " = " + amount);
        saveData();
    }

    public void addCoins(UUID uuid, int amount) {
        loadData();
        int current = getCoins(uuid);
        setCoins(uuid, current + amount);
        saveData();
    }

    public void removeCoins(UUID uuid, int amount) {
        loadData();
        int current = getCoins(uuid);
        setCoins(uuid, Math.max(0, current - amount));
        saveData();
    }

    public void reload() {
        loadData();
        plugin.getLogger().info("金币数据重新加载完成");
    }
}
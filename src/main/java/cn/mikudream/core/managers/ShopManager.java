package cn.mikudream.core.managers;

import cn.mikudream.core.MikuDream;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ShopManager extends BaseManager {
    private static ShopManager instance;
    private static final Map<Material, ShopItem> shopItems = new ConcurrentHashMap<>();
    private final YamlConfiguration shopConfig;
    private final String SHOP_FILE = "shop.yml";

    ShopManager(MikuDream plugin) throws IOException {
        super(plugin);
        this.shopConfig = loadOrCreateYaml(SHOP_FILE);
        loadItems();
    }

    public static synchronized void init(MikuDream plugin) {
        if (instance == null) {
            try {
                instance = new ShopManager(plugin);
            } catch (IOException e) {
                plugin.getLogger().severe("无法初始化ShopManager: " + e.getMessage());
            }
        }
    }

    public @NotNull static ShopManager getInstance() {
        return MikuDream.getInstance().getShopManager();
    }

    private void loadItems() {
        shopItems.clear();
        for (String key : shopConfig.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) continue;

            int buyPrice = shopConfig.getInt(key + ".buyPrice");
            int sellPrice = shopConfig.getInt(key + ".sellPrice");
            String displayName = shopConfig.getString(key + ".displayName", material.name());

            shopItems.put(material, new ShopItem(material, buyPrice, sellPrice, displayName));
        }
    }

    private void saveToConfig() {
        shopItems.forEach((material, item) -> {
            String key = material.name();
            shopConfig.set("%s.buyPrice".formatted(key), item.buyPrice());
            shopConfig.set("%s.sellPrice".formatted(key), item.sellPrice());
            shopConfig.set("%s.displayName".formatted(key), item.displayName());
        });

        try {
            saveYaml(shopConfig, SHOP_FILE);
        } catch (IOException e) {
            handleIOException("保存商店配置文件", e);
        }
    }

    public void addShopItem(Material material, int sellPrice, int buyPrice, String displayName) {
        shopItems.put(material, new ShopItem(material, buyPrice, sellPrice, displayName));
        saveToConfig();
    }

    public static Optional<ShopItem> getShopItem(Material material) {
        return Optional.ofNullable(shopItems.get(material));
    }

    public List<ShopItem> getAllShopItems() {
        return List.copyOf(shopItems.values());
    }

    public boolean isShopItem(ItemStack item) {
        return Optional.ofNullable(item)
                .map(ItemStack::getType)
                .map(shopItems::containsKey)
                .orElse(false);
    }

    public record ShopItem(
            Material material,
            int buyPrice,
            int sellPrice,
            String displayName
    ) {
        public ItemStack createShopItem() {
            return new ItemStack(material, 1);
        }

        public String getItemDisplayName(ItemStack item) {
            return ShopManager.getInstance().getItemDisplayName(item);
        }
    }
}
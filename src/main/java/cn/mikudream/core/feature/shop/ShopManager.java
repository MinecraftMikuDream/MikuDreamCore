package cn.mikudream.core.feature.shop;

import cn.mikudream.core.MikuDream;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ShopManager {
    private static ShopManager instance;
    private static final Map<Material, ShopItem> shopItems = new HashMap<>();
    private final File shopFile;
    private YamlConfiguration shopConfig;

    private ShopManager(MikuDream plugin) {
        shopFile = new File(plugin.getDataFolder(), "shop.yml");
        loadShopConfig();
    }

    public static void init(MikuDream plugin) {
        if (instance == null) {
            instance = new ShopManager(plugin);
        }
    }

    public static ShopManager getInstance() {
        return instance;
    }

    private void loadShopConfig() {
        if (!shopFile.exists()) {
            try {
                shopFile.createNewFile();
                shopConfig = new YamlConfiguration();
            } catch (IOException e) {
                Logger logger = MikuDream.getInstance().getLogger();
                logger.severe("无法创建 shop.yml 文件，请检查插件权限");
            }
        } else {
            shopConfig = YamlConfiguration.loadConfiguration(shopFile);
            loadItems();
        }
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
        for (Map.Entry<Material, ShopItem> entry : shopItems.entrySet()) {
            ShopItem item = entry.getValue();
            String key = item.material().name();
            shopConfig.set(key + ".buyPrice", item.buyPrice());
            shopConfig.set(key + ".sellPrice", item.sellPrice());
            shopConfig.set(key + ".displayName", item.displayName());
        }

        try {
            shopConfig.save(shopFile);
        } catch (IOException e) {
            Logger logger = MikuDream.getInstance().getLogger();
            logger.severe("无法保存 shop.yml 文件，请检查插件权限");
        }
    }

    public void addShopItem(Material material, int sellPrice, int buyPrice, String displayName) {
        shopItems.put(material, new ShopItem(material, buyPrice, sellPrice, displayName));
        saveToConfig();
    }

    public static ShopItem getShopItem(Material material) {
        return shopItems.get(material);
    }

    public Collection<ShopItem> getAllShopItems() {
        return shopItems.values();
    }

    public boolean isShopItem(ItemStack item) {
        return item != null && shopItems.containsKey(item.getType());
    }

    public class ShopItem {
        private final Material material;
        private final int buyPrice;
        private final int sellPrice;
        private final String displayName;

        public ShopItem(Material material, int buyPrice, int sellPrice, String displayName) {
            this.material = material;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.displayName = displayName;
        }

        public ItemStack createShopItem() {
            ItemStack item = new ItemStack(material);
            item.setAmount(1);
            return item;
        }

        public String getDisplayName(ItemStack item) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().getDisplayName();
            }

            return formatMaterialName(item.getType());
        }

        private String formatMaterialName(Material material) {
            String name = material.name().toLowerCase().replace('_', ' ');
            StringBuilder formatted = new StringBuilder();
            for (String word : name.split(" ")) {
                if (!word.isEmpty()) {
                    formatted.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1))
                            .append(" ");
                }
            }
            return formatted.toString().trim();
        }

        public Material material() {
            return material;
        }

        public int buyPrice() {
            return buyPrice;
        }

        public int sellPrice() {
            return sellPrice;
        }

        public String displayName() {
            return displayName;
        }
    }
}
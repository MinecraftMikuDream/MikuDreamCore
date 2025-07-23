package cn.mikudream.core.feature.playermarket;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.shop.ShopManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class PlayerMarketManager {
    private static PlayerMarketManager instance;
    private final Map<UUID, List<MarketItem>> marketItems = new HashMap<>();
    private final File marketFile;
    private YamlConfiguration marketConfig;

    private PlayerMarketManager(MikuDream plugin) {
        marketFile = new File(plugin.getDataFolder(), "player_market.yml");
        loadMarketConfig();
    }

    public static void init(MikuDream plugin) {
        if (instance == null) {
            instance = new PlayerMarketManager(plugin);
        }
    }

    public static PlayerMarketManager getInstance() {
        return instance;
    }

    private void loadMarketConfig() {
        if (!marketFile.exists()) {
            try {
                marketFile.createNewFile();
                marketConfig = new YamlConfiguration();
            } catch (IOException e) {
                Logger logger = MikuDream.getInstance().getLogger();
                logger.warning("Failed to create player market file: " + e.getMessage());
            }
        } else {
            marketConfig = YamlConfiguration.loadConfiguration(marketFile);
            loadItems();
        }
    }

    private void loadItems() {
        marketItems.clear();
        for (String sellerId : marketConfig.getKeys(false)) {
            UUID seller = UUID.fromString(sellerId);
            List<MarketItem> items = new ArrayList<>();

            for (String itemId : marketConfig.getConfigurationSection(sellerId).getKeys(false)) {
                Material material = Material.matchMaterial(Objects.requireNonNull(marketConfig.getString(sellerId + "." + itemId + ".material")));
                int amount = marketConfig.getInt(sellerId + "." + itemId + ".amount");
                int price = marketConfig.getInt(sellerId + "." + itemId + ".price");
                String displayName = marketConfig.getString(sellerId + "." + itemId + ".displayName");

                items.add(new MarketItem(UUID.fromString(itemId), seller, material, amount, price, displayName));
            }

            marketItems.put(seller, items);
        }
    }

    private void saveToConfig() {
        for (Map.Entry<UUID, List<MarketItem>> entry : marketItems.entrySet()) {
            String sellerKey = entry.getKey().toString();
            int index = 0;

            for (MarketItem item : entry.getValue()) {
                String itemKey = sellerKey + "." + item.getItemid().toString();
                marketConfig.set(itemKey + ".material", item.getMaterial().name());
                marketConfig.set(itemKey + ".amount", item.getAmount());
                marketConfig.set(itemKey + ".price", item.getPrice());
                marketConfig.set(itemKey + ".displayName", item.getDisplayName());
                index++;
            }
        }

        try {
            marketConfig.save(marketFile);
        } catch (IOException e) {
            Logger logger = MikuDream.getInstance().getLogger();
            logger.warning("Failed to save player market file: " + e.getMessage());
        }
    }

    public void addMarketItem(Player seller, ItemStack item, int amount, int price) {
        List<MarketItem> sellerItems = marketItems.getOrDefault(seller.getUniqueId(), new ArrayList<>());

        MarketItem marketItem = new MarketItem(
                UUID.randomUUID(),
                seller.getUniqueId(),
                item.getType(),
                amount,
                price,
                getDisplayName(item)
        );

        sellerItems.add(marketItem);
        marketItems.put(seller.getUniqueId(), sellerItems);
        saveToConfig();
    }

    public void removeMarketItem(UUID sellerId, UUID itemId) {
        List<MarketItem> sellerItems = marketItems.get(sellerId);
        if (sellerItems != null) {
            sellerItems.removeIf(item -> item.getItemid().equals(itemId));
            saveToConfig();
        }
    }

    public void updateMarketItem(UUID sellerId, UUID itemId, int newAmount) {
        List<MarketItem> sellerItems = marketItems.get(sellerId);
        if (sellerItems != null) {
            for (MarketItem item : sellerItems) {
                if (item.getItemid().equals(itemId)) {
                    item.setAmount(newAmount);
                    saveToConfig();
                    return;
                }
            }
        }
    }

    public List<MarketItem> getAllMarketItems() {
        List<MarketItem> allItems = new ArrayList<>();
        for (List<MarketItem> sellerItems : marketItems.values()) {
            allItems.addAll(sellerItems);
        }
        return allItems;
    }

    public MarketItem getMarketItem(UUID itemId) {
        for (List<MarketItem> sellerItems : marketItems.values()) {
            for (MarketItem item : sellerItems) {
                if (item.getItemid().equals(itemId)) {
                    return item;
                }
            }
        }
        return null;
    }

    private String getDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }

        return formatMaterialName(item.getType());
    }

    public String formatMaterialName(Material material) {
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

    public static class MarketItem {
        private final UUID itemid;
        private final UUID sellerId; // 新增卖家ID字段
        private final Material material;
        private int amount;
        private final int price;
        private final String displayName;

        public MarketItem(UUID itemid, UUID sellerId, Material material, int amount, int price, String displayName) {
            this.itemid = itemid;
            this.sellerId = sellerId;
            this.material = material;
            this.amount = amount;
            this.price = price;
            this.displayName = displayName;
        }

        public UUID getSellerId() {
            return sellerId;
        }

        public UUID getItemid() {
            return itemid;
        }

        public Material getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getPrice() {
            return price;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ItemStack createItemStack() {
            return new ItemStack(material);
        }
    }
}
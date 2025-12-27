package cn.mikudream.core.managers;

import cn.mikudream.core.MikuDream;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerMarketManager extends BaseManager {
    private static PlayerMarketManager instance;
    private final Map<UUID, List<MarketItem>> marketItems = new ConcurrentHashMap<>();
    private final YamlConfiguration marketConfig;
    private final String MARKET_FILE = "player_market.yml";

    PlayerMarketManager(MikuDream plugin) throws IOException {
        super(plugin);
        this.marketConfig = loadOrCreateYaml(MARKET_FILE);
        loadItems();
    }

    public static synchronized void init(MikuDream plugin) {
        if (instance == null) {
            try {
                instance = new PlayerMarketManager(plugin);
            } catch (IOException e) {
                plugin.getLogger().severe("无法初始化PlayerMarketManager: " + e.getMessage());
            }
        }
    }

    public static PlayerMarketManager getInstance() {
        return MikuDream.getInstance().getPlayerMarketManager();
    }

    private void loadItems() {
        marketItems.clear();
        marketConfig.getKeys(false).forEach(sellerId -> {
            try {
                UUID seller = UUID.fromString(sellerId);
                List<MarketItem> items = new ArrayList<>();

                ConfigurationSection sellerSection = marketConfig.getConfigurationSection(sellerId);
                if (sellerSection != null) {
                    sellerSection.getKeys(false).forEach(itemId -> {
                        String itemPath = "%s.%s".formatted(sellerId, itemId);
                        Map<String, Object> itemData = marketConfig.getConfigurationSection("%s.item".formatted(itemPath))
                                .getValues(false);
                        ItemStack itemStack = ItemStack.deserialize(itemData);

                        int amount = marketConfig.getInt("%s.amount".formatted(itemPath));
                        int price = marketConfig.getInt("%s.price".formatted(itemPath));

                        items.add(new MarketItem(UUID.fromString(itemId), seller, itemStack, amount, price));
                    });
                }

                marketItems.put(seller, items);
            } catch (IllegalArgumentException e) {
                logger.warning("无效的UUID格式: " + sellerId);
            }
        });
    }

    private void saveToConfig() {
        marketItems.forEach((seller, items) -> {
            String sellerKey = seller.toString();
            items.forEach(item -> {
                String itemKey = "%s.%s".formatted(sellerKey, item.itemId());
                marketConfig.set("%s.item".formatted(itemKey), item.sampleItem().serialize());
                marketConfig.set("%s.amount".formatted(itemKey), item.amount());
                marketConfig.set("%s.price".formatted(itemKey), item.price());
            });
        });

        try {
            saveYaml(marketConfig, MARKET_FILE);
        } catch (IOException e) {
            handleIOException("保存玩家市场配置文件", e);
        }
    }

    public void addMarketItem(Player seller, ItemStack item, int amount, int price) {
        MarketItem marketItem = new MarketItem(
                UUID.randomUUID(),
                seller.getUniqueId(),
                item,
                amount,
                price
        );

        marketItems.computeIfAbsent(seller.getUniqueId(), k -> new ArrayList<>())
                .add(marketItem);
        saveToConfig();
    }

    public boolean removeMarketItem(UUID sellerId, UUID itemId) {
        return Optional.ofNullable(marketItems.get(sellerId))
                .map(items -> {
                    boolean removed = items.removeIf(item -> item.itemId().equals(itemId));
                    if (removed) {
                        saveToConfig();
                    }
                    return removed;
                })
                .orElse(false);
    }

    public boolean updateMarketItem(UUID sellerId, UUID itemId, int newAmount) {
        return Optional.ofNullable(marketItems.get(sellerId))
                .flatMap(items -> items.stream()
                        .filter(item -> item.itemId().equals(itemId))
                        .findFirst())
                .map(item -> {
                    MarketItem updated = item.withAmount(newAmount);
                    marketItems.get(sellerId).replaceAll(i ->
                            i.itemId().equals(itemId) ? updated : i);
                    saveToConfig();
                    return true;
                })
                .orElse(false);
    }

    public List<MarketItem> getAllMarketItems() {
        return marketItems.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public Optional<MarketItem> getMarketItem(UUID itemId) {
        return marketItems.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.itemId().equals(itemId))
                .findFirst();
    }

    // 使用记录类，包含with方法用于更新
    public record MarketItem(
            UUID itemId,
            UUID sellerId,
            ItemStack sampleItem,
            int amount,
            int price
    ) {
        public MarketItem {
            sampleItem = sampleItem.clone();
            sampleItem.setAmount(1);
        }

        public MarketItem withAmount(int newAmount) {
            return new MarketItem(itemId, sellerId, sampleItem, newAmount, price);
        }

        public ItemStack createItemStack() {
            return sampleItem.clone();
        }

        public String displayName() {
            return PlayerMarketManager.getInstance().getItemDisplayName(sampleItem);
        }
    }
}
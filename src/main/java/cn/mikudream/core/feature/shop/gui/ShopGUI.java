package cn.mikudream.core.feature.shop.gui;

import cn.mikudream.core.managers.CoinsManager;
import cn.mikudream.core.managers.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShopGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final Map<Integer, ShopManager.ShopItem> itemSlots = new HashMap<>();

    private record PendingTransaction(
            UUID playerId,
            Material material,
            TransactionType type,
            long timestamp
    ) {
        enum TransactionType { PURCHASE, SALE }
    }

    private static final ExecutorService asyncExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("shop-gui-", 0).factory()
    );

    private static final Map<UUID, PendingTransaction> pendingTransactions = new ConcurrentHashMap<>();

    public ShopGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "§#1affd9商§#2eff7e店");
        setupItems();
    }

    private void setupItems() {
        ShopManager.getInstance().getAllShopItems().stream()
                .limit(45)
                .forEachOrdered(shopItem -> {
                    int slot = itemSlots.size();
                    inventory.setItem(slot, createShopItemDisplay(shopItem));
                    itemSlots.put(slot, shopItem);
                });

        inventory.setItem(53, createInfoItem());
    }

    private ItemStack createShopItemDisplay(ShopManager.ShopItem shopItem) {
        ItemStack item = shopItem.createShopItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(shopItem.displayName())
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD));

            meta.lore(List.of(
                    Component.text("左键购买").color(NamedTextColor.GRAY),
                    Component.text("买入价: ")
                            .append(Component.text(shopItem.buyPrice() + " 硬币")
                                    .color(NamedTextColor.WHITE))
                            .color(NamedTextColor.GOLD),
                    Component.text("卖出价: ")
                            .append(Component.text(shopItem.sellPrice() + " 硬币")
                                    .color(NamedTextColor.WHITE))
                            .color(NamedTextColor.GREEN)
            ));

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta meta = infoItem.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("商店帮助")
                    .color(NamedTextColor.YELLOW));

            long playerCoins = CoinsManager.getInstance().getCoins(player.getUniqueId());
            meta.lore(List.of(
                    Component.text("左键点击商品购买").color(NamedTextColor.GRAY),
                    Component.text("购买后会提示输入数量").color(NamedTextColor.GRAY),
                    Component.text("输入数量后完成交易").color(NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("你的硬币: ")
                            .append(Component.text(playerCoins)
                                    .color(NamedTextColor.WHITE))
                            .color(NamedTextColor.GOLD)
            ));

            infoItem.setItemMeta(meta);
        }

        return infoItem;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;

        ShopManager.ShopItem shopItem = itemSlots.get(slot);
        if (shopItem != null) {
            switch (event.getClick()) {
                case LEFT -> handlePurchaseClick(shopItem);
                case RIGHT -> handleSellClick(shopItem);
                default -> {}
            }
        }
    }

    private void handlePurchaseClick(ShopManager.ShopItem shopItem) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        pendingTransactions.put(player.getUniqueId(),
                new PendingTransaction(
                        player.getUniqueId(),
                        shopItem.material(),
                        PendingTransaction.TransactionType.PURCHASE,
                        System.currentTimeMillis()
                ));

        player.sendMessage(Component.text()
                .append(Component.text("请输入购买数量 ")
                        .color(NamedTextColor.GOLD))
                .append(Component.text("(当前硬币: " +
                                CoinsManager.getInstance().getCoins(player.getUniqueId()) + ")")
                        .color(NamedTextColor.WHITE))
                .build());
    }

    private void handleSellClick(ShopManager.ShopItem shopItem) {
        pendingTransactions.put(player.getUniqueId(),
                new PendingTransaction(
                        player.getUniqueId(),
                        shopItem.material(),
                        PendingTransaction.TransactionType.SALE,
                        System.currentTimeMillis()
                ));

        player.closeInventory();

        int available = countItemsInInventory(player, shopItem.material());
        player.sendMessage(Component.text()
                .append(Component.text("请输入要出售的数量 ")
                        .color(NamedTextColor.GOLD))
                .append(Component.text("(背包中有: " + available + " 个)")
                        .color(NamedTextColor.WHITE))
                .build());
    }

    public boolean handleChatInteraction(Player player, String message) {
        PendingTransaction transaction = pendingTransactions.get(player.getUniqueId());
        if (transaction == null) return false;

        // 清理过期交易
        if (System.currentTimeMillis() - transaction.timestamp() > 30000) {
            pendingTransactions.remove(player.getUniqueId());
            return false;
        }

        // 异步处理交易
        asyncExecutor.submit(() -> processTransaction(player, transaction, message));
        return true;
    }

    private void processTransaction(Player player, PendingTransaction transaction, String message) {
        try {
            int amount = Integer.parseInt(message);
            if (amount <= 0) {
                player.sendMessage(Component.text("数量必须大于0").color(NamedTextColor.RED));
                return;
            }

            switch (transaction.type()) {
                case PURCHASE -> processPurchase(player, transaction.material(), amount);
                case SALE -> processSale(player, transaction.material(), amount);
            }

            pendingTransactions.remove(player.getUniqueId());
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("请输入有效的数字数量").color(NamedTextColor.RED));
        }
    }

    private void processPurchase(Player player, Material material, int amount) {
        ShopManager.getShopItem(material).ifPresentOrElse(shopItem -> {
            CoinsManager coinsManager = CoinsManager.getInstance();
            long totalCost = (long) shopItem.buyPrice() * amount;
            long playerCoins = coinsManager.getCoins(player.getUniqueId());

            if (playerCoins < totalCost) {
                player.sendMessage(Component.text()
                        .append(Component.text("硬币不足! 需要 ")
                                .color(NamedTextColor.RED))
                        .append(Component.text(totalCost)
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(", 当前有 ")
                                .color(NamedTextColor.RED))
                        .append(Component.text(playerCoins)
                                .color(NamedTextColor.WHITE))
                        .build());
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(Component.text("背包已满，无法购买物品").color(NamedTextColor.RED));
                return;
            }

            coinsManager.removeCoins(player.getUniqueId(), totalCost);
            player.getInventory().addItem(new ItemStack(material, amount));

            player.sendMessage(Component.text()
                    .append(Component.text("成功购买 ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(amount + " 个 " + shopItem.displayName())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text("，花费 ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(totalCost + " 硬币")
                            .color(NamedTextColor.WHITE))
                    .build());
        }, () -> player.sendMessage(Component.text("该商品已不存在").color(NamedTextColor.RED)));
    }

    private void processSale(Player player, Material material, int amount) {
        ShopManager.getShopItem(material).ifPresentOrElse(shopItem -> {
            int available = countItemsInInventory(player, material);
            if (available < amount) {
                player.sendMessage(Component.text()
                        .append(Component.text("数量不足! 背包中有 ")
                                .color(NamedTextColor.RED))
                        .append(Component.text(available + " 个")
                                .color(NamedTextColor.WHITE))
                        .build());
                return;
            }

            removeItemsFromInventory(player, material, amount);
            int totalEarned = shopItem.sellPrice() * amount;
            CoinsManager.getInstance().addCoins(player.getUniqueId(), totalEarned);

            player.sendMessage(Component.text()
                    .append(Component.text("成功出售 ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(amount + " 个 " + shopItem.displayName())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text("，获得 ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(totalEarned + " 硬币")
                            .color(NamedTextColor.WHITE))
                    .build());
        }, () -> player.sendMessage(Component.text("该商品已不存在").color(NamedTextColor.RED)));
    }

    private static int countItemsInInventory(Player player, Material material) {
        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> item.getType() == material)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    private static void removeItemsFromInventory(Player player, Material material, int amount) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int toRemove = Math.min(item.getAmount(), amount);
                item.setAmount(item.getAmount() - toRemove);
                amount -= toRemove;

                if (amount <= 0) break;
            }
        }
        player.updateInventory();
    }

    // 添加所需依赖
    static {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            Class.forName("net.kyori.adventure.text.format.NamedTextColor");
            Class.forName("net.kyori.adventure.text.format.TextDecoration");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Adventure API not found", e);
        }
    }
}
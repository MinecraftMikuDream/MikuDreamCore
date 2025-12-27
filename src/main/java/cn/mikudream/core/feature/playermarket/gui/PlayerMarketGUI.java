package cn.mikudream.core.feature.playermarket.gui;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.managers.CoinsManager;
import cn.mikudream.core.managers.PlayerMarketManager.MarketItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerMarketGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final int page;
    private final Map<Integer, UUID> itemSlots = new HashMap<>();

    // 使用记录类存储待处理操作
    private record PendingAction(
            UUID playerId,
            ActionType type,
            UUID itemId,
            ItemStack item,
            long timestamp
    ) {
        enum ActionType { PURCHASE, ADDITION }
    }

    private record TransactionResult(
            boolean success,
            String message,
            int newAmount
    ) {}

    // 使用虚拟线程执行异步操作
    private static final ExecutorService asyncExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("market-gui-", 0).factory()
    );

    private static final Map<UUID, PendingAction> pendingActions = new ConcurrentHashMap<>();
    private static final int ITEMS_PER_PAGE = 45;

    public PlayerMarketGUI(Player player, int page) {
        this.player = player;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54,
                "§#ffcd1a玩§#ffb62d家§#ffa03f市§#ff8952场§#ff7265 §#ff5b78-§#ff458a §#ff2e9d第" + (page + 1) + "§#ff2e9d页");
        setupItems();
    }

    private void setupItems() {
        List<MarketItem> allItems = MikuDream.getInstance().getPlayerMarketManager().getAllMarketItems();

        // 分页逻辑
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        // 添加市场物品
        for (int i = startIndex; i < endIndex; i++) {
            MarketItem marketItem = allItems.get(i);
            int slot = i - startIndex;
            inventory.setItem(slot, createMarketItemDisplay(marketItem));
            itemSlots.put(slot, marketItem.itemId());
        }

        // 添加上一页按钮
        if (page > 0) {
            inventory.setItem(45, createNavigationButton("上一页"));
        }

        // 添加下一页按钮
        if ((page + 1) * ITEMS_PER_PAGE < allItems.size()) {
            inventory.setItem(53, createNavigationButton("下一页"));
        }

        // 添加上架物品按钮
        inventory.setItem(49, createAddItemButton());

        // 添加信息项
        inventory.setItem(48, createInfoItem());
    }

    private ItemStack createMarketItemDisplay(MarketItem marketItem) {
        ItemStack displayItem = marketItem.sampleItem().clone();
        ItemMeta meta = displayItem.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(marketItem.displayName())
                    .color(NamedTextColor.GREEN));

            String sellerName = getSellerName(marketItem.sellerId());

            meta.lore(List.of(
                    Component.text("卖家: " + sellerName).color(NamedTextColor.GRAY),
                    Component.text("单价: ")
                            .append(Component.text(marketItem.price() + " 硬币")
                                    .color(NamedTextColor.WHITE))
                            .color(NamedTextColor.GOLD),
                    Component.text("数量: ")
                            .append(Component.text(marketItem.amount())
                                    .color(NamedTextColor.WHITE))
                            .color(NamedTextColor.YELLOW),
                    Component.empty(),
                    Component.text("左键购买").color(NamedTextColor.GREEN)
            ));

            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    private String getSellerName(UUID sellerId) {
        return Optional.ofNullable(Bukkit.getPlayer(sellerId))
                .map(Player::getName)
                .or(() -> Optional.of(Bukkit.getOfflinePlayer(sellerId))
                        .filter(OfflinePlayer::hasPlayedBefore)
                        .map(OfflinePlayer::getName))
                .orElse("未知卖家");
    }

    private ItemStack createNavigationButton(String name) {
        ItemStack button = new ItemStack(Material.ARROW);
        ItemMeta meta = button.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(name).color(NamedTextColor.YELLOW));
            button.setItemMeta(meta);
        }

        return button;
    }

    private ItemStack createAddItemButton() {
        ItemStack addItem = new ItemStack(Material.EMERALD);
        ItemMeta meta = addItem.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("上架物品").color(NamedTextColor.GREEN));
            meta.lore(List.of(
                    Component.text("点击上架手中物品").color(NamedTextColor.GRAY)
            ));
            addItem.setItemMeta(meta);
        }

        return addItem;
    }

    private ItemStack createInfoItem() {
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta meta = infoItem.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("玩家市场帮助").color(NamedTextColor.YELLOW));

            long playerCoins = CoinsManager.getInstance().getCoins(player.getUniqueId());
            meta.lore(List.of(
                    Component.text("左键点击物品购买").color(NamedTextColor.GRAY),
                    Component.text("点击上架物品按钮出售物品").color(NamedTextColor.GRAY),
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

        // 使用模式匹配处理不同的槽位
        switch (slot) {
            case 45 -> navigateToPage(page - 1);  // 上一页
            case 53 -> navigateToPage(page + 1);  // 下一页
            case 49 -> handleAddItemClick(player); // 上架物品
            default -> handleItemClick(slot);      // 物品槽位
        }
    }

    private void navigateToPage(int newPage) {
        player.closeInventory();
        player.openInventory(new PlayerMarketGUI(player, newPage).getInventory());
    }

    private void handleAddItemClick(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(Component.text("请手持要上架的物品").color(NamedTextColor.RED));
            return;
        }

        pendingActions.put(player.getUniqueId(),
                new PendingAction(
                        player.getUniqueId(),
                        PendingAction.ActionType.ADDITION,
                        null,
                        itemInHand.clone(),
                        System.currentTimeMillis()
                ));

        player.closeInventory();
        player.sendMessage(Component.text("请输入上架数量和价格，格式: <数量> <单价>")
                .color(NamedTextColor.GREEN));
        player.sendMessage(Component.text("例如: 64 10").color(NamedTextColor.GRAY));
    }

    private void handleItemClick(int slot) {
        UUID itemId = itemSlots.get(slot);
        if (itemId != null) {
            MikuDream.getInstance().getPlayerMarketManager().getMarketItem(itemId).ifPresent(marketItem -> {
                pendingActions.put(player.getUniqueId(),
                        new PendingAction(
                                player.getUniqueId(),
                                PendingAction.ActionType.PURCHASE,
                                itemId,
                                null,
                                System.currentTimeMillis()
                        ));

                player.closeInventory();
                player.sendMessage(Component.text()
                        .append(Component.text("请输入购买数量 (最大可买: ")
                                .color(NamedTextColor.GOLD))
                        .append(Component.text(marketItem.amount())
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(", 单价: ")
                                .color(NamedTextColor.GOLD))
                        .append(Component.text(marketItem.price() + " 硬币")
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(")"))
                        .build());
            });
        }
    }

    public boolean handleChatInteraction(Player player, String message) {
        PendingAction action = pendingActions.get(player.getUniqueId());
        if (action == null) return false;

        // 清理过期操作
        if (System.currentTimeMillis() - action.timestamp() > 30000) {
            pendingActions.remove(player.getUniqueId());
            return false;
        }

        // 异步处理操作
        asyncExecutor.submit(() -> processAction(player, action, message));
        return true;
    }

    private void processAction(Player player, PendingAction action, String message) {
        switch (action.type()) {
            case PURCHASE -> processPurchase(player, action.itemId(), message);
            case ADDITION -> processAddition(player, action.item(), message);
        }

        pendingActions.remove(player.getUniqueId());
    }

    private void processPurchase(Player player, UUID itemId, String message) {
        MikuDream.getInstance().getPlayerMarketManager().getMarketItem(itemId).ifPresentOrElse(marketItem -> {
            try {
                int amount = Integer.parseInt(message);

                TransactionResult result = validatePurchase(player, marketItem, amount);
                if (!result.success()) {
                    player.sendMessage(Component.text(result.message()).color(NamedTextColor.RED));
                    return;
                }

                executePurchase(player, marketItem, amount);

            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("请输入有效的数字数量").color(NamedTextColor.RED));
            }
        }, () -> player.sendMessage(Component.text("该商品已不存在或已售完").color(NamedTextColor.RED)));
    }

    private TransactionResult validatePurchase(Player player, MarketItem marketItem, int amount) {
        if (amount <= 0) {
            return new TransactionResult(false, "数量必须大于0", 0);
        }

        if (amount > marketItem.amount()) {
            return new TransactionResult(false,
                    "购买数量超过库存! 最大可买: " + marketItem.amount(), 0);
        }

        long totalCost = (long) marketItem.price() * amount;
        long playerCoins = CoinsManager.getInstance().getCoins(player.getUniqueId());

        if (playerCoins < totalCost) {
            return new TransactionResult(false,
                    "硬币不足! 需要 " + totalCost + ", 当前有 " + playerCoins, 0);
        }

        if (player.getInventory().firstEmpty() == -1) {
            return new TransactionResult(false, "背包已满，无法购买物品", 0);
        }

        int remaining = marketItem.amount() - amount;
        return new TransactionResult(true, "验证通过", remaining);
    }

    private void executePurchase(Player player, MarketItem marketItem, int amount) {
        long totalCost = (long) marketItem.price() * amount;

        // 执行交易
        CoinsManager.getInstance().removeCoins(player.getUniqueId(), totalCost);

        ItemStack purchasedItem = marketItem.sampleItem().clone();
        purchasedItem.setAmount(amount);
        player.getInventory().addItem(purchasedItem);

        // 给卖家加钱
        CoinsManager.getInstance().addCoins(marketItem.sellerId(), totalCost);

        // 通知卖家（如果在线）
        Optional.ofNullable(Bukkit.getPlayer(marketItem.sellerId()))
                .ifPresent(seller -> seller.sendMessage(Component.text()
                        .append(Component.text(player.getName())
                                .color(NamedTextColor.YELLOW))
                        .append(Component.text(" 购买了你的 ")
                                .color(NamedTextColor.GREEN))
                        .append(Component.text(marketItem.displayName())
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(" x" + amount)
                                .color(NamedTextColor.GREEN))
                        .append(Component.text("，获得 ")
                                .color(NamedTextColor.GREEN))
                        .append(Component.text(totalCost + " 硬币")
                                .color(NamedTextColor.WHITE))
                        .build()));

        // 更新市场物品
        int remaining = marketItem.amount() - amount;
        if (remaining <= 0) {
            MikuDream.getInstance().getPlayerMarketManager().removeMarketItem(marketItem.sellerId(), marketItem.itemId());
        } else {
            MikuDream.getInstance().getPlayerMarketManager().updateMarketItem(marketItem.sellerId(), marketItem.itemId(), remaining);
        }

        player.sendMessage(Component.text()
                .append(Component.text("成功购买 ")
                        .color(NamedTextColor.GREEN))
                .append(Component.text(amount + " 个 " + marketItem.displayName())
                        .color(NamedTextColor.WHITE))
                .append(Component.text("，花费 ")
                        .color(NamedTextColor.GREEN))
                .append(Component.text(totalCost + " 硬币")
                        .color(NamedTextColor.WHITE))
                .build());
    }

    private void processAddition(Player player, ItemStack itemToSell, String message) {
        String[] parts = message.split(" ");
        if (parts.length != 2) {
            player.sendMessage(Component.text("格式错误! 请输入: <数量> <单价>")
                    .color(NamedTextColor.RED));
            return;
        }

        try {
            int amount = Integer.parseInt(parts[0]);
            int price = Integer.parseInt(parts[1]);

            if (amount <= 0 || price <= 0) {
                player.sendMessage(Component.text("数量和价格必须大于0")
                        .color(NamedTextColor.RED));
                return;
            }

            // 检查玩家是否有足够的物品
            int available = countItemsInInventory(player, itemToSell.getType());
            if (available < amount) {
                player.sendMessage(Component.text()
                        .append(Component.text("物品数量不足! 背包中有 ")
                                .color(NamedTextColor.RED))
                        .append(Component.text(available + " 个")
                                .color(NamedTextColor.WHITE))
                        .build());
                return;
            }

            // 移除物品
            removeItemsFromInventory(player, itemToSell.getType(), amount);

            // 添加到市场
            MikuDream.getInstance().getPlayerMarketManager().addMarketItem(player, itemToSell, amount, price);

            player.sendMessage(Component.text()
                    .append(Component.text("成功上架 ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(amount + " 个 ")
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(itemToSell.getType().toString())
                            .color(NamedTextColor.YELLOW))
                    .append(Component.text("，单价: ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(price + " 硬币")
                            .color(NamedTextColor.WHITE))
                    .build());

        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("请输入有效的数字")
                    .color(NamedTextColor.RED));
        }
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
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Adventure API not found", e);
        }
    }
}
package cn.mikudream.core.feature.shop.gui;

import cn.mikudream.core.managers.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdminShopGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final Map<Integer, ShopManager.ShopItem> itemSlots = new HashMap<>();

    // 使用记录类存储管理员操作
    private record AdminAction(
            Player player,
            Material material,
            ActionType type,
            long timestamp
    ) {
        enum ActionType { ADD, EDIT }
    }

    private static final Map<UUID, AdminAction> pendingActions = new ConcurrentHashMap<>();

    // 使用密封接口定义GUI操作
    private sealed interface GUIAction {
        record EditPrice(ShopManager.ShopItem item) implements GUIAction {}
        record AddItem(ItemStack handItem) implements GUIAction {}
        record Close() implements GUIAction {}
    }

    public AdminShopGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "§#ffcd1a商§#ff9846店§#ff6371管§#ff2e9d理");
        setupItems();
    }

    private void setupItems() {
        // 添加现有商店物品
        ShopManager.getInstance().getAllShopItems().stream()
                .limit(45)
                .forEachOrdered(shopItem -> {
                    int slot = itemSlots.size();
                    inventory.setItem(slot, createShopItemDisplay(shopItem));
                    itemSlots.put(slot, shopItem);
                });

        // 添加控制按钮
        inventory.setItem(51, createAddItemButton());
        inventory.setItem(53, createDoneButton());
    }

    private ItemStack createShopItemDisplay(ShopManager.ShopItem shopItem) {
        ItemStack item = shopItem.createShopItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(shopItem.displayName())
                    .color(NamedTextColor.GREEN));

            meta.lore(List.of(
                    Component.text("左键编辑价格").color(NamedTextColor.GRAY),
                    Component.text("买入价: ")
                            .append(Component.text(shopItem.buyPrice())
                                    .color(NamedTextColor.WHITE))
                            .color(NamedTextColor.GOLD),
                    Component.text("卖出价: ")
                            .append(Component.text(shopItem.sellPrice())
                                    .color(NamedTextColor.WHITE))
                            .color(NamedTextColor.GREEN)
            ));

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createAddItemButton() {
        ItemStack addItem = new ItemStack(Material.ANVIL);
        ItemMeta meta = addItem.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("添加新物品")
                    .color(NamedTextColor.GREEN));

            meta.lore(List.of(
                    Component.text("手持物品点击此按钮").color(NamedTextColor.GRAY),
                    Component.text("添加新商品到商店").color(NamedTextColor.GRAY)
            ));

            addItem.setItemMeta(meta);
        }

        return addItem;
    }

    private ItemStack createDoneButton() {
        ItemStack doneItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = doneItem.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("完成")
                    .color(NamedTextColor.RED));

            doneItem.setItemMeta(meta);
        }

        return doneItem;
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

        GUIAction action = switch (slot) {
            case 51 -> handleAddItemClick();
            case 53 -> new GUIAction.Close();
            default -> handleItemSlotClick(slot);
        };

        processAction(action);
    }

    private GUIAction handleAddItemClick() {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            player.sendMessage(Component.text("请手持物品点击添加按钮").color(NamedTextColor.RED));
            return null;
        }

        Material material = handItem.getType();
        if (ShopManager.getShopItem(material).isPresent()) {
            player.sendMessage(Component.text("该物品已在商店中存在").color(NamedTextColor.RED));
            return null;
        }

        return new GUIAction.AddItem(handItem);
    }

    private GUIAction handleItemSlotClick(int slot) {
        ShopManager.ShopItem shopItem = itemSlots.get(slot);
        if (shopItem != null) {
            return new GUIAction.EditPrice(shopItem);
        }
        return null;
    }

    private void processAction(GUIAction action) {
        if (action == null) return;

        // 使用模式匹配处理不同类型的动作
        switch (action) {
            case GUIAction.EditPrice editPrice -> {
                pendingActions.put(player.getUniqueId(),
                        new AdminAction(player, editPrice.item().material(),
                                AdminAction.ActionType.EDIT, System.currentTimeMillis()));
                player.closeInventory();
                player.sendMessage(Component.text()
                        .append(Component.text("请输入 "))
                        .append(Component.text(editPrice.item().displayName())
                                .color(NamedTextColor.YELLOW))
                        .append(Component.text(" 的新价格，格式: <买入价> <卖出价>"))
                        .color(NamedTextColor.GREEN)
                        .build());
                player.sendMessage(Component.text("例如: 120 90")
                        .color(NamedTextColor.GRAY));
            }
            case GUIAction.AddItem addItem -> {
                pendingActions.put(player.getUniqueId(),
                        new AdminAction(player, addItem.handItem().getType(),
                                AdminAction.ActionType.ADD, System.currentTimeMillis()));
                player.closeInventory();
                player.sendMessage(Component.text()
                        .append(Component.text("请输入该物品的买入价和卖出价，格式: <买入价> <卖出价>")
                                .color(NamedTextColor.GREEN))
                        .build());
                player.sendMessage(Component.text("例如: 100 80")
                        .color(NamedTextColor.GRAY));
            }
            case GUIAction.Close ignored -> player.closeInventory();
        }
    }

    public boolean handleChatInteraction(Player player, String message) {
        AdminAction action = pendingActions.get(player.getUniqueId());
        if (action == null) return false;

        // 清理过期操作
        if (System.currentTimeMillis() - action.timestamp() > 30000) {
            pendingActions.remove(player.getUniqueId());
            return false;
        }

        String[] parts = message.split(" ");
        if (parts.length != 2) {
            player.sendMessage(Component.text("格式错误! 请输入: <买入价> <卖出价>")
                    .color(NamedTextColor.RED));
            return true;
        }

        try {
            int buyPrice = Integer.parseInt(parts[0]);
            int sellPrice = Integer.parseInt(parts[1]);

            if (buyPrice <= 0 || sellPrice <= 0) {
                player.sendMessage(Component.text("价格必须大于0")
                        .color(NamedTextColor.RED));
                return true;
            }

            if (sellPrice > buyPrice) {
                player.sendMessage(Component.text("卖出价不能高于买入价")
                        .color(NamedTextColor.RED));
                return true;
            }

            // 添加或更新商品
            String displayName = getDisplayName(action.material());
            ShopManager.getInstance().addShopItem(
                    action.material(),
                    sellPrice,
                    buyPrice,
                    displayName
            );

            player.sendMessage(Component.text()
                    .append(Component.text("成功设置 ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(action.material().name())
                            .color(NamedTextColor.YELLOW))
                    .append(Component.text(" 价格: 买入=")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(buyPrice)
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(", 卖出=")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(sellPrice)
                            .color(NamedTextColor.WHITE))
                    .build());

            pendingActions.remove(player.getUniqueId());
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("请输入有效的数字价格")
                    .color(NamedTextColor.RED));
            return true;
        }
    }

    private String getDisplayName(Material material) {
        return Arrays.stream(material.name().toLowerCase().split("_"))
                .map(word -> !word.isEmpty()
                        ? Character.toUpperCase(word.charAt(0)) + word.substring(1)
                        : "")
                .collect(java.util.stream.Collectors.joining(" "));
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
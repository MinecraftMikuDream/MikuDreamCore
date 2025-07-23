package cn.mikudream.core.feature.shop.gui;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class ShopGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final Map<Integer, ShopManager.ShopItem> itemSlots = new HashMap<>();
    private static final Map<UUID, Material> pendingPurchases = new HashMap<>();
    private static final Map<UUID, Material> pendingSales = new HashMap<>();

    public ShopGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + "商店");
        setupItems();
    }

    private void setupItems() {
        int slot = 0;
        for (ShopManager.ShopItem shopItem : ShopManager.getInstance().getAllShopItems()) {
            ItemStack item = shopItem.createShopItem();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + shopItem.displayName());
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "左键购买",
                        ChatColor.GOLD + "买入价: " + ChatColor.WHITE + shopItem.buyPrice() + " 硬币",
                        ChatColor.GREEN + "卖出价: " + ChatColor.WHITE + shopItem.sellPrice() + " 硬币"
                ));
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            itemSlots.put(slot, shopItem);
            slot++;
        }

        // 添加信息项
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.YELLOW + "商店帮助");
            infoMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "左键点击商品购买",
                    ChatColor.GRAY + "购买后会提示输入数量",
                    ChatColor.GRAY + "输入数量后完成交易",
                    "",
                    ChatColor.GOLD + "你的硬币: " + ChatColor.WHITE +
                            new CoinsManager(MikuDream.getInstance()).getCoins(player.getUniqueId())
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(53, infoItem);
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
            if (event.isLeftClick()) {
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                pendingPurchases.put(player.getUniqueId(), shopItem.material());
                player.sendMessage(ChatColor.GOLD + "请输入购买数量 (当前硬币: " +
                        new CoinsManager(MikuDream.getInstance()).getCoins(player.getUniqueId()) + ")");
            }
            else if (event.isRightClick()) {
                handleRightClickSell((Player) event.getWhoClicked(), Objects.requireNonNull(event.getCurrentItem()));
            }
        }
    }

    public static boolean handlePurchase(Player player, String message) {
        if (pendingPurchases.containsKey(player.getUniqueId())) {
            Material material = pendingPurchases.get(player.getUniqueId());
            ShopManager.ShopItem shopItem = ShopManager.getInstance().getShopItem(material);

            if (shopItem == null) {
                player.sendMessage(ChatColor.RED + "该商品已不存在，请重新打开商店");
                pendingPurchases.remove(player.getUniqueId());
                return true;
            }

            try {
                int amount = Integer.parseInt(message);
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "数量必须大于0");
                    return true;
                }

                CoinsManager coinsManager = new CoinsManager(MikuDream.getInstance());
                int totalCost = shopItem.buyPrice() * amount;
                int playerCoins = coinsManager.getCoins(player.getUniqueId());

                if (playerCoins < totalCost) {
                    player.sendMessage(ChatColor.RED + "硬币不足! 需要 " + totalCost +
                            ", 当前有 " + playerCoins);
                    pendingPurchases.remove(player.getUniqueId());
                    return true;
                }

                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "背包已满，无法购买物品");
                    pendingPurchases.remove(player.getUniqueId());
                    return true;
                }

                coinsManager.removeCoins(player.getUniqueId(), totalCost);
                player.getInventory().addItem(new ItemStack(material, amount));

                player.sendMessage(String.format(ChatColor.GREEN + "成功购买 %d 个 %s，花费 %d 硬币",
                        amount, shopItem.displayName(), totalCost));

                pendingPurchases.remove(player.getUniqueId());
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "请输入有效的数字数量");
                return true;
            }
        }
        return false;
    }

    public void handleRightClickSell(Player player, ItemStack clickedItem) {
        Material material = clickedItem.getType();
        ShopManager.ShopItem shopItem = ShopManager.getInstance().getShopItem(material);

        if (shopItem == null) {
            player.sendMessage(ChatColor.RED + "该物品不能在商店出售");
            return;
        }

        pendingSales.put(player.getUniqueId(), material);
        player.closeInventory();

        player.sendMessage(ChatColor.GOLD + "请输入要出售的数量 (背包中有: " +
                countItemsInInventory(player, material) + " 个)");
    }

    private static int countItemsInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static boolean handleSale(Player player, String message) {
        if (pendingSales.containsKey(player.getUniqueId())) {
            Material material = pendingSales.get(player.getUniqueId());
            ShopManager.ShopItem shopItem = ShopManager.getInstance().getShopItem(material);

            if (shopItem == null) {
                player.sendMessage(ChatColor.RED + "该商品已不存在，无法出售");
                pendingSales.remove(player.getUniqueId());
                return true;
            }

            try {
                int amount = Integer.parseInt(message);
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "数量必须大于0");
                    return true;
                }

                int available = countItemsInInventory(player, material);
                if (available < amount) {
                    player.sendMessage(ChatColor.RED + "数量不足! 背包中有 " + available + " 个");
                    pendingSales.remove(player.getUniqueId());
                    return true;
                }

                // 移除物品
                removeItemsFromInventory(player, material, amount);

                // 计算收益
                int totalEarned = shopItem.sellPrice() * amount;
                CoinsManager coinsManager = new CoinsManager(MikuDream.getInstance());
                coinsManager.addCoins(player.getUniqueId(), totalEarned);

                player.sendMessage(String.format(ChatColor.GREEN + "成功出售 %d 个 %s，获得 %d 硬币",
                        amount, shopItem.displayName(), totalEarned));

                pendingSales.remove(player.getUniqueId());
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "请输入有效的数字数量");
                return true;
            }
        }
        return false;
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
}
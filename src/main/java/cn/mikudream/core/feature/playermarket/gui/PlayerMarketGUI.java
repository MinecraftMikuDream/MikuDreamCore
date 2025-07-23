package cn.mikudream.core.feature.playermarket.gui;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.playermarket.PlayerMarketManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class PlayerMarketGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final int page;
    private final Map<Integer, UUID> itemSlots = new HashMap<>();
    private static final Map<UUID, UUID> pendingPurchases = new HashMap<>();
    private static final Map<UUID, ItemStack> pendingAdditions = new HashMap<>();

    private static final int ITEMS_PER_PAGE = 45;

    public PlayerMarketGUI(Player player, int page) {
        this.player = player;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.GOLD + "玩家市场 - 第" + (page+1) + "页");
        setupItems();
    }

    private void setupItems() {
        List<PlayerMarketManager.MarketItem> allItems = PlayerMarketManager.getInstance().getAllMarketItems();

        // 分页逻辑
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        // 添加市场物品
        for (int i = startIndex; i < endIndex; i++) {
            PlayerMarketManager.MarketItem marketItem = allItems.get(i);
            ItemStack item = marketItem.createItemStack();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + marketItem.getDisplayName());

                String sellerName = "未知卖家";
                Player seller = Bukkit.getPlayer(marketItem.getSellerId()); // 使用 getSellerId()
                if (seller != null) {
                    sellerName = seller.getName();
                } else {
                    OfflinePlayer offlineSeller = Bukkit.getOfflinePlayer(marketItem.getSellerId());
                    if (offlineSeller.hasPlayedBefore()) {
                        sellerName = offlineSeller.getName();
                    }
                }

                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "卖家: " + sellerName,
                        ChatColor.GOLD + "单价: " + ChatColor.WHITE + marketItem.getPrice() + " 硬币",
                        ChatColor.YELLOW + "数量: " + ChatColor.WHITE + marketItem.getAmount(),
                        "",
                        ChatColor.GREEN + "左键购买"
                ));
                item.setItemMeta(meta);
            }

            inventory.setItem(i - startIndex, item);
            itemSlots.put(i - startIndex, marketItem.getItemid());
        }

        // 添加上一页按钮
        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "上一页");
            prevPage.setItemMeta(prevMeta);
            inventory.setItem(45, prevPage);
        }

        // 添加下一页按钮
        if ((page + 1) * ITEMS_PER_PAGE < allItems.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "下一页");
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(53, nextPage);
        }

        // 添加上架物品按钮
        ItemStack addItem = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "上架物品");
        addMeta.setLore(Collections.singletonList(ChatColor.GRAY + "点击上架手中物品"));
        addItem.setItemMeta(addMeta);
        inventory.setItem(49, addItem);

        // 添加信息项
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "玩家市场帮助");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "左键点击物品购买",
                ChatColor.GRAY + "点击上架物品按钮出售物品",
                "",
                ChatColor.GOLD + "你的硬币: " + ChatColor.WHITE +
                        new CoinsManager(MikuDream.getInstance()).getCoins(player.getUniqueId())
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(48, infoItem);
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

        // 翻页处理
        if (slot == 45) { // 上一页
            player.closeInventory();
            player.openInventory(new PlayerMarketGUI(player, page - 1).getInventory());
            return;
        } else if (slot == 53) { // 下一页
            player.closeInventory();
            player.openInventory(new PlayerMarketGUI(player, page + 1).getInventory());
            return;
        }

        // 上架物品按钮
        if (slot == 49) {
            if (handleAddItem((Player) event.getWhoClicked())) {
                event.setCancelled(true);
                return;
            }
            return;
        }

        UUID itemId = itemSlots.get(slot);
        if (itemId != null) {
            PlayerMarketManager.MarketItem marketItem = PlayerMarketManager.getInstance().getMarketItem(itemId);
            if (marketItem != null) {
                pendingPurchases.put(player.getUniqueId(), itemId);
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "请输入购买数量 (最大可买: " + marketItem.getAmount() +
                        ", 单价: " + marketItem.getPrice() + " 硬币)");
            }
        }
    }

    public static boolean handleAddItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "请手持要上架的物品");
            return true;
        }

        pendingAdditions.put(player.getUniqueId(), itemInHand.clone());
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "请输入上架数量和价格，格式: <数量> <单价>");
        player.sendMessage(ChatColor.GRAY + "例如: 64 10");
        return false;
    }

    public static boolean handlePlayerMarketPurchase(Player player, String message) {
        if (pendingPurchases.containsKey(player.getUniqueId())) {
            UUID itemId = pendingPurchases.get(player.getUniqueId());
            PlayerMarketManager.MarketItem marketItem = PlayerMarketManager.getInstance().getMarketItem(itemId);

            if (marketItem == null) {
                player.sendMessage(ChatColor.RED + "该商品已不存在或已售完");
                pendingPurchases.remove(player.getUniqueId());
                return true;
            }

            try {
                int amount = Integer.parseInt(message);
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "数量必须大于0");
                    return true;
                }

                if (amount > marketItem.getAmount()) {
                    player.sendMessage(ChatColor.RED + "购买数量超过库存! 最大可买: " + marketItem.getAmount());
                    return true;
                }

                // 计算总价
                int totalCost = marketItem.getPrice() * amount;
                CoinsManager coinsManager = new CoinsManager(MikuDream.getInstance());
                int playerCoins = coinsManager.getCoins(player.getUniqueId());

                if (playerCoins < totalCost) {
                    player.sendMessage(ChatColor.RED + "硬币不足! 需要 " + totalCost +
                            ", 当前有 " + playerCoins);
                    pendingPurchases.remove(player.getUniqueId());
                    return true;
                }

                // 检查背包空间
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "背包已满，无法购买物品");
                    pendingPurchases.remove(player.getUniqueId());
                    return true;
                }

                // 扣除买家硬币
                coinsManager.removeCoins(player.getUniqueId(), totalCost);

                // 给予买家物品
                ItemStack purchasedItem = new ItemStack(marketItem.getMaterial(), amount);
                player.getInventory().addItem(purchasedItem);

                // 给卖家转账
                Player seller = Bukkit.getPlayer(marketItem.getItemid());
                if (seller != null && seller.isOnline()) {
                    coinsManager.addCoins(seller.getUniqueId(), totalCost);
                    seller.sendMessage(ChatColor.GREEN + player.getName() + " 购买了你的 " +
                            marketItem.getDisplayName() + " x" + amount +
                            "，获得 " + totalCost + " 硬币");
                } else {
                    // 如果卖家不在线，保存到离线数据
                    coinsManager.addCoins(marketItem.getItemid(), totalCost);
                }

                // 更新市场物品
                int remaining = marketItem.getAmount() - amount;
                if (remaining <= 0) {
                    PlayerMarketManager.getInstance().removeMarketItem(marketItem.getSellerId(), marketItem.getItemid());
                } else {
                    marketItem.setAmount(remaining);
                    PlayerMarketManager.getInstance().updateMarketItem(marketItem.getSellerId(), marketItem.getItemid(), remaining);
                }

                player.sendMessage(String.format(ChatColor.GREEN + "成功购买 %d 个 %s，花费 %d 硬币",
                        amount, marketItem.getDisplayName(), totalCost));

                pendingPurchases.remove(player.getUniqueId());
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "请输入有效的数字数量");
                return true;
            }
        }
        return false;
    }

    public static boolean handlePlayerMarketAddition(Player player, String message) {
        if (pendingAdditions.containsKey(player.getUniqueId())) {
            ItemStack itemToSell = pendingAdditions.get(player.getUniqueId());

            String[] parts = message.split(" ");
            if (parts.length != 2) {
                player.sendMessage(ChatColor.RED + "格式错误! 请输入: <数量> <单价>");
                return true;
            }

            try {
                int amount = Integer.parseInt(parts[0]);
                int price = Integer.parseInt(parts[1]);

                if (amount <= 0 || price <= 0) {
                    player.sendMessage(ChatColor.RED + "数量和价格必须大于0");
                    return true;
                }

                // 检查玩家是否有足够的物品
                int available = countItemsInInventory(player, itemToSell.getType());
                if (available < amount) {
                    player.sendMessage(ChatColor.RED + "物品数量不足! 背包中有 " + available + " 个");
                    pendingAdditions.remove(player.getUniqueId());
                    return true;
                }

                // 移除物品
                removeItemsFromInventory(player, itemToSell.getType(), amount);

                // 添加到市场
                PlayerMarketManager.getInstance().addMarketItem(player, itemToSell, amount, price);

                player.sendMessage(ChatColor.GREEN + "成功上架 " + amount + " 个 " +
                        PlayerMarketManager.getInstance().formatMaterialName(itemToSell.getType()) +
                        "，单价: " + price + " 硬币");

                pendingAdditions.remove(player.getUniqueId());
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "请输入有效的数字");
                return true;
            }
        }
        return false;
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
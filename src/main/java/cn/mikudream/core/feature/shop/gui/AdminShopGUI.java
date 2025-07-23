package cn.mikudream.core.feature.shop.gui;

import cn.mikudream.core.feature.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdminShopGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final Map<Integer, ShopManager.ShopItem> itemSlots = new HashMap<>();
    private static final Map<Player, Material> pendingPriceSet = new HashMap<>();

    public AdminShopGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.RED + "商店管理");
        setupItems();
    }

    private void setupItems() {
        // 添加现有商店物品
        int slot = 0;
        for (ShopManager.ShopItem shopItem : ShopManager.getInstance().getAllShopItems()) {
            ItemStack item = shopItem.createShopItem();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + shopItem.displayName());
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "左键编辑价格",
                        ChatColor.GOLD + "买入价: " + ChatColor.WHITE + shopItem.buyPrice(),
                        ChatColor.GREEN + "卖出价: " + ChatColor.WHITE + shopItem.sellPrice()
                ));
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            itemSlots.put(slot, shopItem);
            slot++;
        }

        // 添加添加物品按钮
        ItemStack addItem = new ItemStack(Material.ANVIL);
        ItemMeta addMeta = addItem.getItemMeta();
        if (addMeta != null) {
            addMeta.setDisplayName(ChatColor.GREEN + "添加新物品");
            addMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "手持物品点击此按钮",
                    ChatColor.GRAY + "添加新商品到商店"
            ));
            addItem.setItemMeta(addMeta);
        }
        inventory.setItem(51, addItem);

        // 添加完成按钮
        ItemStack doneItem = new ItemStack(Material.BARRIER);
        ItemMeta doneMeta = doneItem.getItemMeta();
        if (doneMeta != null) {
            doneMeta.setDisplayName(ChatColor.RED + "完成");
            doneItem.setItemMeta(doneMeta);
        }
        inventory.setItem(53, doneItem);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;
        if (slot == 51) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "请手持物品点击添加按钮");
                return;
            }

            Material material = handItem.getType();
            if (ShopManager.getShopItem(material) != null) {
                player.sendMessage(ChatColor.RED + "该物品已在商店中存在");
                return;
            }

            pendingPriceSet.put(player, material);
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "请输入该物品的买入价和卖出价，格式: <买入价> <卖出价>");
            player.sendMessage(ChatColor.GRAY + "例如: 100 80");
        }
        else if (slot == 53) { // 完成按钮
            player.closeInventory();
        }
        else {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            ShopManager.ShopItem shopItem = itemSlots.get(slot);
            if (shopItem != null) {
                pendingPriceSet.put(player, shopItem.material());
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "请输入 " + shopItem.getDisplayName(handItem) +

                        " 的新价格，格式: <买入价> <卖出价>");
                player.sendMessage(ChatColor.GRAY + "例如: 120 90");
            }
        }
    }

    public static boolean handleAdminCommand(Player player, String message) {
        if (pendingPriceSet.containsKey(player)) {
            Material material = pendingPriceSet.get(player);

            String[] parts = message.split(" ");
            if (parts.length != 2) {
                player.sendMessage(ChatColor.RED + "格式错误! 请输入: <买入价> <卖出价>");
                return true;
            }

            try {
                int buyPrice = Integer.parseInt(parts[0]);
                int sellPrice = Integer.parseInt(parts[1]);

                if (buyPrice <= 0 || sellPrice <= 0) {
                    player.sendMessage(ChatColor.RED + "价格必须大于0");
                    return true;
                }

                if (sellPrice > buyPrice) {
                    player.sendMessage(ChatColor.RED + "卖出价不能高于买入价");
                    return true;
                }

                // 添加或更新商品
                ShopManager.getInstance().addShopItem(
                        material,
                        sellPrice,
                        buyPrice,
                        material.name()
                );

                player.sendMessage(ChatColor.GREEN + "成功设置 " + material.name() +
                        " 价格: 买入=" + buyPrice + ", 卖出=" + sellPrice);

                pendingPriceSet.remove(player);
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "请输入有效的数字价格");
                return true;
            }
        }
        return false;
    }
}
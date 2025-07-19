package cn.mikudream.core.command.impl;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.scoin.SCoinManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class SRFCommand implements CommandExecutor, Listener {
    private final MikuDream plugin;
    private final SCoinManager coinManager;
    private final int lotteryCost = 50;

    // GUI标题常量
    private static final String NORMAL_LOTTERY_TITLE = ChatColor.GOLD + "普通抽奖";
    private static final String PUNISHMENT_LOTTERY_TITLE = ChatColor.DARK_RED + "惩罚抽奖!";

    public SRFCommand(MikuDream plugin, SCoinManager coinManager) {
        this.plugin = plugin;
        this.coinManager = coinManager;

        // 注册事件监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String @NotNull [] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令!");
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        int scoin = coinManager.getCoins(uuid);
        if (scoin < lotteryCost) {
            player.sendMessage(ChatColor.RED + "您的金币不足喵! 需要 " + lotteryCost + " 硬币.");
            return false;
        }

        handleNormalLottery(player);
        return true;
    }

    // 防止玩家从抽奖GUI中取出物品
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // 检查是否是抽奖GUI
        if (title.equals(NORMAL_LOTTERY_TITLE) || title.equals(PUNISHMENT_LOTTERY_TITLE)) {
            // 取消所有点击操作
            event.setCancelled(true);

            // 如果是玩家自己的背包，允许操作
            if (event.getRawSlot() >= event.getInventory().getSize()) {
                return;
            }

            // 播放点击声音效果
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
    }

    // 防止玩家拖动抽奖GUI中的物品
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(NORMAL_LOTTERY_TITLE) || title.equals(PUNISHMENT_LOTTERY_TITLE)) {
            event.setCancelled(true);
        }
    }

    public void handleNormalLottery(Player player) {
        coinManager.removeCoins(player.getUniqueId(), lotteryCost);

        Inventory lotteryGui = Bukkit.createInventory(player, 27, NORMAL_LOTTERY_TITLE);
        ItemStack border = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");

        // 填充边界
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                lotteryGui.setItem(i, border);
            }
        }

        // 创建奖品
        lotteryGui.setItem(10, createGuiItem(Material.NETHERITE_BLOCK, ChatColor.GOLD + "硬币666"));
        lotteryGui.setItem(12, createGuiItem(Material.DIAMOND_BLOCK, ChatColor.AQUA + "硬币100"));
        lotteryGui.setItem(14, createGuiItem(Material.END_CRYSTAL, ChatColor.GREEN + "再抽一次吧"));
        lotteryGui.setItem(16, createGuiItem(Material.TNT, ChatColor.RED + "噩耗"));
        lotteryGui.setItem(18, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "看什么看这不是奖励"));

        player.openInventory(lotteryGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return; // 确保玩家在线

            int random = (int) (Math.random() * 100);
            if (random < 1) { // 1% 获得666硬币
                player.closeInventory();
                coinManager.addCoins(player.getUniqueId(), 666);
                player.sendMessage(ChatColor.GOLD + "恭喜获得666硬币!");
            }
            else if (random < 15) { // 15% 获得100硬币
                player.closeInventory();
                coinManager.addCoins(player.getUniqueId(), 100);
                player.sendMessage(ChatColor.AQUA + "恭喜获得100硬币!");
            }
            else if (random < 30) { // 15% 抽到再抽一次
                player.closeInventory();
                handleNormalLottery(player);
            }
            else if (random < 70) { // 40% 抽到TNT，触发惩罚抽奖
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "噢不! 你抽中了噩耗!");
                handlePunishmentLottery(player); // 立即开始惩罚抽奖
            }
            else { // 30% 未中奖
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "很遗憾您没有中奖喵!");
            }
        }, 60L); // 3秒延迟
    }

    // 惩罚抽奖方法
    public void handlePunishmentLottery(Player player) {
        Inventory punishmentGui = Bukkit.createInventory(player, 27, PUNISHMENT_LOTTERY_TITLE);
        ItemStack border = createGuiItem(Material.RED_STAINED_GLASS_PANE, " ");

        // 填充边界
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                punishmentGui.setItem(i, border);
            }
        }

        // 创建惩罚奖品
        punishmentGui.setItem(10, createGuiItem(Material.DIAMOND_SWORD, ChatColor.RED + "死亡之剑"));
        punishmentGui.setItem(11, createGuiItem(Material.OBSIDIAN, ChatColor.DARK_PURPLE + "深渊传送"));
        punishmentGui.setItem(12, createGuiItem(Material.POISONOUS_POTATO, ChatColor.DARK_GREEN + "中毒效果"));
        punishmentGui.setItem(13, createGuiItem(Material.CHEST, ChatColor.DARK_RED + "背包掉落"));
        punishmentGui.setItem(14, createGuiItem(Material.WITHER_SKELETON_SKULL, ChatColor.BLACK + "凋零诅咒"));
        punishmentGui.setItem(15, createGuiItem(Material.FIRE_CHARGE, ChatColor.GOLD + "引火上身"));
        punishmentGui.setItem(16, createGuiItem(Material.ZOMBIE_HEAD, ChatColor.GREEN + "召唤僵尸"));

        player.openInventory(punishmentGui);

        // 3秒后显示惩罚结果
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return; // 确保玩家在线

            player.closeInventory();
            int random = (int) (Math.random() * 100);

            if (random < 15) { // 15% 死亡
                player.setHealth(0);
                player.sendMessage(ChatColor.DARK_RED + "死亡之剑刺穿了你!");
            }
            else if (random < 30) { // 15% 传送至深渊
                Location loc = player.getLocation().clone();
                loc.setY(-100);
                player.teleport(loc);
                player.sendMessage(ChatColor.DARK_PURPLE + "你被传送到了深渊!");
            }
            else if (random < 45) { // 15% 中毒
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
                player.sendMessage(ChatColor.DARK_GREEN + "你感到全身不适...中毒了!");
            }
            else if (random < 60) { // 15% 背包物品掉落
                // 掉落背包所有物品
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                    }
                }
                player.getInventory().clear();
                player.sendMessage(ChatColor.DARK_RED + "你的背包物品全部掉出来了!");
            }
            else if (random < 75) { // 15% 凋零效果
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1));
                player.sendMessage(ChatColor.BLACK + "你被凋零诅咒了!");
            }
            else if (random < 90) { // 15% 着火
                player.setFireTicks(200);
                player.sendMessage(ChatColor.GOLD + "你着火了!");
            }
            else { // 10% 召唤僵尸
                Location loc = player.getLocation();
                for (int i = 0; i < 5; i++) {
                    loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.ZOMBIE);
                }
                player.sendMessage(ChatColor.GREEN + "僵尸大军出现了!");
            }
        }, 60L);
    }

    public void handleDangerLottery(Player player) {
        handlePunishmentLottery(player);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}
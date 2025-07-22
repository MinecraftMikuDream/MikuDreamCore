package cn.mikudream.core.command.impl;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.coin.CoinsManager;
import com.google.common.hash.Hashing;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class SRFCommand implements CommandExecutor, Listener {
    private final MikuDream plugin;
    private final CoinsManager coinManager;
    private final int lotteryCost = 288;
    SecureRandom secureRandom = new SecureRandom();

    // GUI标题常量
    private static final String NORMAL_LOTTERY_TITLE = ChatColor.GOLD + "普通抽奖";
    private static final String PUNISHMENT_LOTTERY_TITLE = ChatColor.DARK_RED + "惩罚抽奖!";
    private static final String MINERAL_LOTTERY_TITLE = ChatColor.BLUE + "矿物抽奖";
    private static final String EQUIPMENT_LOTTERY_TITLE = ChatColor.DARK_PURPLE + "装备抽奖";
    private static final String BLOCK_LOTTERY_TITLE = ChatColor.DARK_PURPLE + "方块抽奖";

    public SRFCommand(MikuDream plugin, CoinsManager coinManager) {
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // 检查是否是抽奖GUI
        if (title.equals(NORMAL_LOTTERY_TITLE) || title.equals(PUNISHMENT_LOTTERY_TITLE) ||
                title.equals(MINERAL_LOTTERY_TITLE) || title.equals(EQUIPMENT_LOTTERY_TITLE) || title.equals(BLOCK_LOTTERY_TITLE)) {

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

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(NORMAL_LOTTERY_TITLE) || title.equals(PUNISHMENT_LOTTERY_TITLE) ||
                title.equals(MINERAL_LOTTERY_TITLE) || title.equals(EQUIPMENT_LOTTERY_TITLE)) {
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
        lotteryGui.setItem(11, createGuiItem(Material.DIAMOND_BLOCK, ChatColor.AQUA + "硬币100"));
        lotteryGui.setItem(12, createGuiItem(Material.END_CRYSTAL, ChatColor.GREEN + "再抽一次"));
        lotteryGui.setItem(13, createGuiItem(Material.TNT, ChatColor.RED + "噩耗"));
        lotteryGui.setItem(14, createGuiItem(Material.DIAMOND_ORE, ChatColor.BLUE + "矿物抽奖券"));
        lotteryGui.setItem(15, createGuiItem(Material.IRON_SWORD, ChatColor.LIGHT_PURPLE + "装备抽奖券"));
        lotteryGui.setItem(16, createGuiItem(Material.GOLD_BLOCK, ChatColor.GOLD + "方块抽奖券"));

        player.openInventory(lotteryGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return; // 确保玩家在线


            double randomValue = secureRandom.nextDouble() * 100;
            if (randomValue < 1.0) { // 1% 获得666硬币
                player.closeInventory();
                coinManager.addCoins(player.getUniqueId(), 666);
                player.sendMessage(ChatColor.GOLD + "恭喜获得666硬币!");
            }
            else if (randomValue < 16.0) { // 15% 获得100硬币
                player.closeInventory();
                coinManager.addCoins(player.getUniqueId(), 100);
                player.sendMessage(ChatColor.AQUA + "恭喜获得100硬币!");
            }
            else if (randomValue < 26.0) { // 10% 抽到再抽一次
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "恭喜获得再抽一次!");
                handleNormalLottery(player);
            }
            else if (randomValue < 36.0) { // 10% 获得矿物抽奖券
                player.closeInventory();
                player.sendMessage(ChatColor.BLUE + "恭喜获得矿物抽奖券!");
                handleMineralLottery(player);
            }
            else if (randomValue < 46.0) { // 10% 获得装备抽奖券
                player.closeInventory();
                player.sendMessage(ChatColor.LIGHT_PURPLE + "恭喜获得装备抽奖券!");
                handleEquipmentLottery(player);
            }
            else if (randomValue < 56.0) { // 10% 获得方块抽奖券
                player.closeInventory();
                player.sendMessage(ChatColor.BLUE + "恭喜获得方块抽奖券!");
                handleBlockLottery(player);
            }
            else if (randomValue < 66.0) { // 30% 抽到TNT，触发惩罚抽奖
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "噢不! 你抽中了噩耗!");
                handlePunishmentLottery(player); // 立即开始惩罚抽奖
            }
            else { // 34% 未中奖
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "很遗憾您没有中奖喵!");
            }
        }, 60L); // 3秒延迟
    }

    private void handleBlockLottery(Player player) {
        Inventory mineralGui = Bukkit.createInventory(player, 27, MINERAL_LOTTERY_TITLE);
        ItemStack border = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                mineralGui.setItem(i, border);
            }
        }

        mineralGui.setItem(10, createGuiItem(Material.COAL_BLOCK, ChatColor.BLACK + "煤块"));
        mineralGui.setItem(11, createGuiItem(Material.IRON_BLOCK, ChatColor.GRAY + "铁块"));
        mineralGui.setItem(12, createGuiItem(Material.GOLD_BLOCK, ChatColor.GOLD + "金块"));
        mineralGui.setItem(13, createGuiItem(Material.DIAMOND_BLOCK, ChatColor.AQUA + "钻石块"));
        mineralGui.setItem(14, createGuiItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "绿宝石块"));
        mineralGui.setItem(15, createGuiItem(Material.NETHERITE_BLOCK, ChatColor.WHITE + "下界合金块"));
        mineralGui.setItem(16, createGuiItem(Material.REDSTONE_BLOCK, ChatColor.DARK_RED + "红石块"));

        player.openInventory(mineralGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            player.closeInventory();
            double randomValue = secureRandom.nextDouble() * 100;
            Material mineralType;
            int amount;
            if (randomValue < 20.0) {
                mineralType = Material.COAL_BLOCK;
                amount = 3 + secureRandom.nextInt(6);
            } else if (randomValue < 40.0) {
                mineralType = Material.IRON_BLOCK;
                amount = 2 + secureRandom.nextInt(4);
            } else if (randomValue < 60.0) {
                mineralType = Material.GOLD_BLOCK;
                amount = 1 + secureRandom.nextInt(4);
            } else if (randomValue < 75.0) {
                mineralType = Material.DIAMOND_BLOCK;
                amount = 1 + secureRandom.nextInt(3);
            } else if (randomValue < 85.0) {
                mineralType = Material.EMERALD_BLOCK;
                amount = 1 + secureRandom.nextInt(2);
            } else if (randomValue < 95.0) {
                mineralType = Material.NETHERITE_BLOCK;
                amount = 1;
            } else {
                mineralType = Material.REDSTONE_BLOCK;
                amount = 1 + secureRandom.nextInt(2);
            }

            ItemStack mineralReward = new ItemStack(mineralType, amount);
            player.getInventory().addItem(mineralReward);
            player.sendMessage(ChatColor.BLUE + "恭喜获得 " + amount + " 个 " + mineralType.name() + "!");
        }, 60L);
    }

    public void handleMineralLottery(Player player) {
        Inventory mineralGui = Bukkit.createInventory(player, 27, MINERAL_LOTTERY_TITLE);
        ItemStack border = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                mineralGui.setItem(i, border);
            }
        }

        mineralGui.setItem(10, createGuiItem(Material.COAL_ORE, ChatColor.BLACK + "煤矿石"));
        mineralGui.setItem(11, createGuiItem(Material.IRON_ORE, ChatColor.GRAY + "铁矿石"));
        mineralGui.setItem(12, createGuiItem(Material.GOLD_ORE, ChatColor.GOLD + "金矿石"));
        mineralGui.setItem(13, createGuiItem(Material.DIAMOND_ORE, ChatColor.AQUA + "钻石矿石"));
        mineralGui.setItem(14, createGuiItem(Material.EMERALD_ORE, ChatColor.GREEN + "绿宝石矿石"));
        mineralGui.setItem(15, createGuiItem(Material.NETHER_QUARTZ_ORE, ChatColor.WHITE + "下界石英矿石"));
        mineralGui.setItem(16, createGuiItem(Material.ANCIENT_DEBRIS, ChatColor.DARK_RED + "远古残骸"));

        player.openInventory(mineralGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            player.closeInventory();
            double randomValue = secureRandom.nextDouble() * 100;

            Material mineralType;
            int amount;

            if (randomValue < 20.0) {
                mineralType = Material.COAL_ORE;
                amount = 3 + secureRandom.nextInt(6);
            } else if (randomValue < 40.0) {
                mineralType = Material.IRON_ORE;
                amount = 2 + secureRandom.nextInt(4);
            } else if (randomValue < 60.0) {
                mineralType = Material.GOLD_ORE;
                amount = 1 + secureRandom.nextInt(4);
            } else if (randomValue < 75.0) {
                mineralType = Material.DIAMOND_ORE;
                amount = 1 + secureRandom.nextInt(3);
            } else if (randomValue < 85.0) {
                mineralType = Material.EMERALD_ORE;
                amount = 1 + secureRandom.nextInt(2);
            } else if (randomValue < 95.0) {
                mineralType = Material.NETHER_QUARTZ_ORE;
                amount = 3 + secureRandom.nextInt(6);
            } else {
                mineralType = Material.ANCIENT_DEBRIS;
                amount = 1 + secureRandom.nextInt(2);
            }

            ItemStack mineralReward = new ItemStack(mineralType, amount);
            player.getInventory().addItem(mineralReward);
            player.sendMessage(ChatColor.BLUE + "恭喜获得 " + amount + " 个 " + mineralType.name() + "!");
        }, 60L);
    }

    public void handleEquipmentLottery(Player player) {
        Inventory equipmentGui = Bukkit.createInventory(player, 27, EQUIPMENT_LOTTERY_TITLE);
        ItemStack border = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " ");

        // 填充边界
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                equipmentGui.setItem(i, border);
            }
        }

        // 创建装备奖品
        equipmentGui.setItem(10, createGuiItem(Material.IRON_SWORD, ChatColor.GRAY + "铁剑"));
        equipmentGui.setItem(11, createGuiItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "钻石剑"));
        equipmentGui.setItem(12, createGuiItem(Material.IRON_CHESTPLATE, ChatColor.GRAY + "铁胸甲"));
        equipmentGui.setItem(13, createGuiItem(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "钻石胸甲"));
        equipmentGui.setItem(14, createGuiItem(Material.BOW, ChatColor.GOLD + "弓"));
        equipmentGui.setItem(15, createGuiItem(Material.CROSSBOW, ChatColor.DARK_RED + "弩"));
        equipmentGui.setItem(16, createGuiItem(Material.ELYTRA, ChatColor.LIGHT_PURPLE + "鞘翅"));

        player.openInventory(equipmentGui);

        // 3秒后显示装备结果
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            player.closeInventory();
            double randomValue = secureRandom.nextDouble() * 100;

            // 随机装备
            ItemStack equipmentReward;

            if (randomValue < 25.0) { // 25% 铁剑
                equipmentReward = new ItemStack(Material.IRON_SWORD);
            } else if (randomValue < 45.0) { // 20% 钻石剑
                equipmentReward = new ItemStack(Material.DIAMOND_SWORD);
            } else if (randomValue < 60.0) { // 15% 铁胸甲
                equipmentReward = new ItemStack(Material.IRON_CHESTPLATE);
            } else if (randomValue < 75.0) { // 15% 钻石胸甲
                equipmentReward = new ItemStack(Material.DIAMOND_CHESTPLATE);
            } else if (randomValue < 90.0) { // 10% 弓
                equipmentReward = new ItemStack(Material.BOW);
            } else if (randomValue < 95.0) { // 5% 弩
                equipmentReward = new ItemStack(Material.CROSSBOW);
            } else { // 5% 鞘翅
                equipmentReward = new ItemStack(Material.ELYTRA);
            }

            // 给予玩家装备
            player.getInventory().addItem(equipmentReward);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "恭喜获得 " + equipmentReward.getType().name() + "!");
        }, 60L);
    }

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
        punishmentGui.setItem(12, createGuiItem(Material.POISONOUS_POTATO, ChatColor.DARK_GREEN + "倾家荡产"));
        punishmentGui.setItem(13, createGuiItem(Material.CHEST, ChatColor.DARK_RED + "背包掉落"));
        punishmentGui.setItem(14, createGuiItem(Material.WITHER_SKELETON_SKULL, ChatColor.BLACK + "凋零诅咒"));
        punishmentGui.setItem(15, createGuiItem(Material.FIRE_CHARGE, ChatColor.GOLD + "引火上身"));
        punishmentGui.setItem(16, createGuiItem(Material.ZOMBIE_HEAD, ChatColor.GREEN + "召唤僵尸"));

        player.openInventory(punishmentGui);

        // 3秒后显示惩罚结果
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return; // 确保玩家在线

            player.closeInventory();
            double randomValue = secureRandom.nextDouble() * 100;

            if (randomValue < 15.0) { // 15% 死亡
                player.setHealth(0);
                player.sendMessage(ChatColor.DARK_RED + "死亡之剑刺穿了你!");
            }
            else if (randomValue < 30.0) { // 15% 传送至深渊
                Location loc = player.getLocation().clone();
                loc.setY(-100);
                player.teleport(loc);
                player.sendMessage(ChatColor.DARK_PURPLE + "你被传送到了深渊!");
            }
            else if (randomValue < 45.0) { // 15% 倾家荡产
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
                coinManager.removeCoins(player.getUniqueId(), 500);
                player.sendMessage(ChatColor.DARK_GREEN + "你感到眩晕...你被扣除500个币了!");
            }
            else if (randomValue < 60.0) { // 15% 背包物品掉落
                // 掉落背包所有物品
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                    }
                }
                player.getInventory().clear();
                player.sendMessage(ChatColor.DARK_RED + "你的背包物品全部掉出来了!");
            }
            else if (randomValue < 75.0) { // 15% 凋零效果
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1));
                player.sendMessage(ChatColor.BLACK + "你被凋零诅咒了!");
            }
            else if (randomValue < 90.0) { // 15% 着火
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
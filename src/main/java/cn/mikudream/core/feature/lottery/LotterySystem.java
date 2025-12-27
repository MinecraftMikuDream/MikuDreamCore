package cn.mikudream.core.feature.lottery;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.managers.CoinsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 完整的抽奖系统
 */
public final class LotterySystem implements Listener {
    private final MikuDream plugin;
    private final CoinsManager coinsManager;
    private final SecureRandom random = new SecureRandom();
    private final Set<UUID> activeLotteries = ConcurrentHashMap.newKeySet();

    private static final int LOTTERY_COST = 288;

    // GUI标题
    private static final String NORMAL_LOTTERY_TITLE =  "§#ffcb0f普§#f8bd40通§#f2b071抽§#eba2a2奖";
    private static final String PUNISHMENT_LOTTERY_TITLE = "§#ff0f0f惩§#f43838罚§#ea6262抽§#df8b8b奖";
    private static final String MINERAL_LOTTERY_TITLE = "§#0fffe3矿§#4bf4c6物§#86eaa8抽§#c2df8b奖";
    private static final String EQUIPMENT_LOTTERY_TITLE = "§#bf0fff装§#ca38e9备§#d462d2抽§#df8bbc奖";
    private static final String BLOCK_LOTTERY_TITLE = "§#0fff83方§#45f486块§#7bea88抽§#b1df8b奖";

    public LotterySystem(MikuDream plugin, CoinsManager coinsManager) {
        this.plugin = plugin;
        this.coinsManager = coinsManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean startNormalLottery(Player player) {
        UUID playerId = player.getUniqueId();

        // 检查硬币
        long playerCoins = coinsManager.getCoins(playerId);
        if (playerCoins < LOTTERY_COST) {
            player.sendMessage("§c你的硬币不足! 需要 " + LOTTERY_COST + " 硬币");
            return false;
        }

        // 扣除硬币
        coinsManager.removeCoins(playerId, LOTTERY_COST);
        activeLotteries.add(playerId);

        // 创建GUI
        Inventory lotteryGui = createLotteryGUI(player, NORMAL_LOTTERY_TITLE, List.of(
                new LotteryItem(Material.NETHERITE_BLOCK, "§6硬币666"),
                new LotteryItem(Material.DIAMOND_BLOCK, "§b硬币100"),
                new LotteryItem(Material.END_CRYSTAL, "§a再抽一次"),
                new LotteryItem(Material.TNT, "§4噩耗"),
                new LotteryItem(Material.DIAMOND_ORE, "§9矿物抽奖券"),
                new LotteryItem(Material.IRON_SWORD, "§5装备抽奖券"),
                new LotteryItem(Material.GOLD_BLOCK, "§6方块抽奖券")
        ));

        player.openInventory(lotteryGui);

        // 3秒后执行抽奖
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !activeLotteries.contains(playerId)) {
                return;
            }

            player.closeInventory();
            activeLotteries.remove(playerId);

            double randomValue = random.nextDouble() * 100;

            if (randomValue < 1.0) { // 1% 获得666硬币
                coinsManager.addCoins(playerId, 666);
                player.sendMessage("§6恭喜获得666硬币!");
            } else if (randomValue < 16.0) { // 15% 获得100硬币
                coinsManager.addCoins(playerId, 100);
                player.sendMessage("§b恭喜获得100硬币!");
            } else if (randomValue < 26.0) { // 10% 再抽一次
                player.sendMessage("§a恭喜获得再抽一次!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> startNormalLottery(player), 20L);
            } else if (randomValue < 36.0) { // 10% 矿物抽奖券
                player.sendMessage("§9恭喜获得矿物抽奖券!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> startMineralLottery(player), 20L);
            } else if (randomValue < 46.0) { // 10% 装备抽奖券
                player.sendMessage("§#0fe3ff恭§#1de3f9喜§#2be2f2获§#38e2ec得§#46e1e6装§#54e1df备§#62e0d9抽§#6fe0d3奖§#7ddfcc券§#8bdfc6!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> startEquipmentLottery(player), 20L);
            } else if (randomValue < 56.0) { // 10% 方块抽奖券
                player.sendMessage("§#0fe3ff恭§#1de3f9喜§#2be2f2获§#38e2ec得§#46e1e6方§#54e1df块§#62e0d9抽§#6fe0d3奖§#7ddfcc券§#8bdfc6!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> startBlockLottery(player), 20L);
            } else if (randomValue < 66.0) { // 30% 惩罚抽奖
                player.sendMessage("§#ff0f0f噢§#fc1b1d不§#f9282b!§#f53439 §#f24147你§#ef4d56抽§#ec5964中§#e96672了§#e57280噩§#e27f8e耗§#df8b9c!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> startPunishmentLottery(player), 20L);
            } else { // 34% 未中奖
                player.sendMessage("§#ff0f0f很§#fb1f21遗§#f72e32憾§#f33e44您§#ef4d56没§#eb5d67有§#e76c79中§#e37c8a奖§#df8b9c!");
            }
        }, 60L); // 3秒延迟

        return true;
    }

    public void startPunishmentLottery(Player player) {
        UUID playerId = player.getUniqueId();
        activeLotteries.add(playerId);

        Inventory punishmentGui = createLotteryGUI(player, PUNISHMENT_LOTTERY_TITLE, List.of(
                new LotteryItem(Material.DIAMOND_SWORD, "§4死亡之剑"),
                new LotteryItem(Material.OBSIDIAN, "§5深渊传送"),
                new LotteryItem(Material.POISONOUS_POTATO, "§2倾家荡产"),
                new LotteryItem(Material.CHEST, "§4背包掉落"),
                new LotteryItem(Material.WITHER_SKELETON_SKULL, "§0凋零诅咒"),
                new LotteryItem(Material.FIRE_CHARGE, "§6引火上身"),
                new LotteryItem(Material.ZOMBIE_HEAD, "§2召唤僵尸")
        ));

        player.openInventory(punishmentGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !activeLotteries.contains(playerId)) {
                return;
            }

            player.closeInventory();
            activeLotteries.remove(playerId);

            double randomValue = random.nextDouble() * 100;

            if (randomValue < 15.0) { // 15% 死亡
                player.setHealth(0);
                player.sendMessage("§4死亡之剑刺穿了你!");
            } else if (randomValue < 30.0) { // 15% 传送至深渊
                Location loc = player.getLocation().clone();
                loc.setY(-100);
                player.teleport(loc);
                player.sendMessage("§5你被传送到了深渊!");
            } else if (randomValue < 45.0) { // 15% 倾家荡产
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
                coinsManager.removeCoins(playerId, 500);
                player.sendMessage("§2你感到眩晕...你被扣除500硬币了!");
            } else if (randomValue < 60.0) { // 15% 背包物品掉落
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                    }
                }
                player.getInventory().clear();
                player.sendMessage("§4你的背包物品全部掉出来了!");
            } else if (randomValue < 75.0) { // 15% 凋零效果
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1));
                player.sendMessage("§0你被凋零诅咒了!");
            } else if (randomValue < 90.0) { // 15% 着火
                player.setFireTicks(200);
                player.sendMessage("§6你着火了!");
            } else { // 10% 召唤僵尸
                Location loc = player.getLocation();
                for (int i = 0; i < 5; i++) {
                    loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.ZOMBIE);
                }
                player.sendMessage("§2僵尸大军出现了!");
            }
        }, 60L);
    }

    public void startMineralLottery(Player player) {
        UUID playerId = player.getUniqueId();
        activeLotteries.add(playerId);

        Inventory mineralGui = createLotteryGUI(player, MINERAL_LOTTERY_TITLE, List.of(
                new LotteryItem(Material.COAL_ORE, "§0煤矿石"),
                new LotteryItem(Material.IRON_ORE, "§7铁矿石"),
                new LotteryItem(Material.GOLD_ORE, "§6金矿石"),
                new LotteryItem(Material.DIAMOND_ORE, "§b钻石矿石"),
                new LotteryItem(Material.EMERALD_ORE, "§a绿宝石矿石"),
                new LotteryItem(Material.NETHER_QUARTZ_ORE, "§f下界石英矿石"),
                new LotteryItem(Material.ANCIENT_DEBRIS, "§4远古残骸")
        ));

        player.openInventory(mineralGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !activeLotteries.contains(playerId)) {
                return;
            }

            player.closeInventory();
            activeLotteries.remove(playerId);

            double randomValue = random.nextDouble() * 100;
            Material mineralType;
            int amount;

            if (randomValue < 20.0) {
                mineralType = Material.COAL_ORE;
                amount = 3 + random.nextInt(6);
            } else if (randomValue < 40.0) {
                mineralType = Material.IRON_ORE;
                amount = 2 + random.nextInt(4);
            } else if (randomValue < 60.0) {
                mineralType = Material.GOLD_ORE;
                amount = 1 + random.nextInt(4);
            } else if (randomValue < 75.0) {
                mineralType = Material.DIAMOND_ORE;
                amount = 1 + random.nextInt(3);
            } else if (randomValue < 85.0) {
                mineralType = Material.EMERALD_ORE;
                amount = 1 + random.nextInt(2);
            } else if (randomValue < 95.0) {
                mineralType = Material.NETHER_QUARTZ_ORE;
                amount = 3 + random.nextInt(6);
            } else {
                mineralType = Material.ANCIENT_DEBRIS;
                amount = 1 + random.nextInt(2);
            }

            ItemStack mineralReward = new ItemStack(mineralType, amount);
            player.getInventory().addItem(mineralReward);
            player.sendMessage("§9恭喜获得 " + amount + " 个 " + mineralType.name() + "!");
        }, 60L);
    }

    public void startEquipmentLottery(Player player) {
        UUID playerId = player.getUniqueId();
        activeLotteries.add(playerId);

        Inventory equipmentGui = createLotteryGUI(player, EQUIPMENT_LOTTERY_TITLE, List.of(
                new LotteryItem(Material.IRON_SWORD, "§7铁剑"),
                new LotteryItem(Material.DIAMOND_SWORD, "§b钻石剑"),
                new LotteryItem(Material.IRON_CHESTPLATE, "§7铁胸甲"),
                new LotteryItem(Material.DIAMOND_CHESTPLATE, "§b钻石胸甲"),
                new LotteryItem(Material.BOW, "§6弓"),
                new LotteryItem(Material.CROSSBOW, "§4弩"),
                new LotteryItem(Material.ELYTRA, "§5鞘翅")
        ));

        player.openInventory(equipmentGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !activeLotteries.contains(playerId)) {
                return;
            }

            player.closeInventory();
            activeLotteries.remove(playerId);

            double randomValue = random.nextDouble() * 100;
            ItemStack equipmentReward;

            if (randomValue < 25.0) {
                equipmentReward = new ItemStack(Material.IRON_SWORD);
            } else if (randomValue < 45.0) {
                equipmentReward = new ItemStack(Material.DIAMOND_SWORD);
            } else if (randomValue < 60.0) {
                equipmentReward = new ItemStack(Material.IRON_CHESTPLATE);
            } else if (randomValue < 75.0) {
                equipmentReward = new ItemStack(Material.DIAMOND_CHESTPLATE);
            } else if (randomValue < 90.0) {
                equipmentReward = new ItemStack(Material.BOW);
            } else if (randomValue < 95.0) {
                equipmentReward = new ItemStack(Material.CROSSBOW);
            } else {
                equipmentReward = new ItemStack(Material.ELYTRA);
            }

            player.getInventory().addItem(equipmentReward);
            player.sendMessage("§5恭喜获得 " + equipmentReward.getType().name() + "!");
        }, 60L);
    }

    public void startBlockLottery(Player player) {
        UUID playerId = player.getUniqueId();
        activeLotteries.add(playerId);

        Inventory blockGui = createLotteryGUI(player, BLOCK_LOTTERY_TITLE, List.of(
                new LotteryItem(Material.COAL_BLOCK, "§0煤块"),
                new LotteryItem(Material.IRON_BLOCK, "§7铁块"),
                new LotteryItem(Material.GOLD_BLOCK, "§6金块"),
                new LotteryItem(Material.DIAMOND_BLOCK, "§b钻石块"),
                new LotteryItem(Material.EMERALD_BLOCK, "§a绿宝石块"),
                new LotteryItem(Material.NETHERITE_BLOCK, "§f下界合金块"),
                new LotteryItem(Material.REDSTONE_BLOCK, "§4红石块")
        ));

        player.openInventory(blockGui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !activeLotteries.contains(playerId)) {
                return;
            }

            player.closeInventory();
            activeLotteries.remove(playerId);

            double randomValue = random.nextDouble() * 100;
            Material blockType;
            int amount;

            if (randomValue < 20.0) {
                blockType = Material.COAL_BLOCK;
                amount = 3 + random.nextInt(6);
            } else if (randomValue < 40.0) {
                blockType = Material.IRON_BLOCK;
                amount = 2 + random.nextInt(4);
            } else if (randomValue < 60.0) {
                blockType = Material.GOLD_BLOCK;
                amount = 1 + random.nextInt(4);
            } else if (randomValue < 75.0) {
                blockType = Material.DIAMOND_BLOCK;
                amount = 1 + random.nextInt(3);
            } else if (randomValue < 85.0) {
                blockType = Material.EMERALD_BLOCK;
                amount = 1 + random.nextInt(2);
            } else if (randomValue < 95.0) {
                blockType = Material.NETHERITE_BLOCK;
                amount = 1;
            } else {
                blockType = Material.REDSTONE_BLOCK;
                amount = 1 + random.nextInt(2);
            }

            ItemStack blockReward = new ItemStack(blockType, amount);
            player.getInventory().addItem(blockReward);
            player.sendMessage("§9恭喜获得 " + amount + " 个 " + blockType.name() + "!");
        }, 60L);
    }

    private Inventory createLotteryGUI(Player player, String title, List<LotteryItem> items) {
        Inventory gui = Bukkit.createInventory(null, 27, title);
        ItemStack border = createBorderItem();

        // 填充边界
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, border);
            }
        }

        // 添加奖品
        for (int i = 0; i < Math.min(items.size(), 7); i++) {
            LotteryItem lotteryItem = items.get(i);
            ItemStack item = new ItemStack(lotteryItem.material());
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(lotteryItem.displayName());
                item.setItemMeta(meta);
            }

            gui.setItem(10 + i, item);
        }

        return gui;
    }

    private ItemStack createBorderItem() {
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
        }
        return border;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(NORMAL_LOTTERY_TITLE) || title.equals(PUNISHMENT_LOTTERY_TITLE) ||
                title.equals(MINERAL_LOTTERY_TITLE) || title.equals(EQUIPMENT_LOTTERY_TITLE) ||
                title.equals(BLOCK_LOTTERY_TITLE)) {

            event.setCancelled(true);

            // 如果是玩家自己的背包，允许操作
            if (event.getRawSlot() >= event.getInventory().getSize()) {
                return;
            }

            // 播放点击声音
            if (event.getWhoClicked() instanceof Player player) {
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(NORMAL_LOTTERY_TITLE) || title.equals(PUNISHMENT_LOTTERY_TITLE) ||
                title.equals(MINERAL_LOTTERY_TITLE) || title.equals(EQUIPMENT_LOTTERY_TITLE) ||
                title.equals(BLOCK_LOTTERY_TITLE)) {
            event.setCancelled(true);
        }
    }

    private record LotteryItem(Material material, String displayName) {}
}
package cn.mikudream.core.feature.protection;

import cn.mikudream.core.MikuDream;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 特殊物品保护和粒子效果系统
 */
public class SpecialItemProtection implements Listener {
    private final MikuDream plugin;
    private final NamespacedKey specialItemKey;
    private final Set<Location> activeEffects = ConcurrentHashMap.newKeySet();

    // 物品特征（根据你提供的NBT）
    private static final String SPECIAL_ITEM_NAME = "啊~你怎么能~到处~奖励呢~";
    private static final List<String> SPECIAL_ITEM_LORE = Arrays.asList(
            "想要吗来自爱卿的奖赏吗~",
            "主人~拿在副手上~有很神奇的效果呢~",
            "",
            "+91 攻击伤害"
    );

    public SpecialItemProtection(MikuDream plugin) {
        this.plugin = plugin;
        this.specialItemKey = new NamespacedKey(plugin, "special_endrod");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (isSpecialItem(item)) {
            // 阻止放置
            event.setCancelled(true);

            Player player = event.getPlayer();
            Block block = event.getBlock();
            Location location = block.getLocation().add(0.5, 0.5, 0.5);

            // 发送提示消息
            player.sendActionBar(Component.text()
                    .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                    .append(Component.text("? 这个物品不能放置！"))
                    .build());

            // 播放音效
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);

            // 显示粒子效果
            showParticleRing(location);

            // 如果玩家有权限，可以给予提示
            if (player.hasPermission("special.item.bypass")) {
                player.sendMessage(Component.text()
                        .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                        .append(Component.text("提示：这是一个特殊物品，具有强大的能量，不能随意放置。"))
                        .build());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检测玩家是否尝试使用特殊物品
        if (event.getItem() != null && isSpecialItem(event.getItem())) {
            // 如果是右键点击方块（尝试放置）
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                // 我们已经通过 BlockPlaceEvent 处理了，这里可以添加额外效果
                Player player = event.getPlayer();

                // 显示手持特殊物品的粒子效果
                new BukkitRunnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        if (count++ >= 20) { // 显示1秒
                            cancel();
                            return;
                        }

                        // 在手部位置显示粒子
                        Location handLocation = player.getLocation().clone();
                        handLocation.setYaw(handLocation.getYaw() + 90);
                        handLocation.setPitch(handLocation.getPitch() + 90);

                        double angle = Math.toRadians(handLocation.getYaw());
                        double offsetX = Math.cos(angle) * 0.5;
                        double offsetZ = Math.sin(angle) * 0.5;

                        Location particleLoc = player.getLocation().clone()
                                .add(offsetX, 1.2, offsetZ);

                        player.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                particleLoc,
                                3,
                                0.1, 0.1, 0.1,
                                0,
                                new Particle.DustTransition(
                                        Color.fromRGB(255, 0, 255),  // 紫色
                                        Color.fromRGB(128, 0, 128),  // 深紫色
                                        1.0f
                                )
                        );
                    }
                }.runTaskTimer(plugin, 0, 1);
            }
        }
    }

    private boolean isSpecialItem(ItemStack item) {
        if (item == null || item.getType() != Material.END_ROD) {
            return false;
        }

        // 方法1：检查PersistentData
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(specialItemKey, PersistentDataType.BYTE)) {
                return true;
            }

            // 方法2：检查显示名称和Lore
            if (meta.hasDisplayName() && meta.hasLore()) {
                String displayName = meta.getDisplayName();
                List<String> lore = meta.getLore();

                // 检查名称
                boolean nameMatches = displayName.contains(SPECIAL_ITEM_NAME) ||
                        displayName.equalsIgnoreCase("§d啊~你怎么能~到处~奖励呢~");

                // 检查Lore
                boolean loreMatches = false;
                if (lore != null && lore.size() >= 4) {
                    boolean hasLore1 = lore.stream().anyMatch(line ->
                            line.contains("想要吗来自爱卿的奖赏吗~"));
                    boolean hasLore2 = lore.stream().anyMatch(line ->
                            line.contains("主人~拿在副手上~有很神奇的效果呢~"));
                    boolean hasLore4 = lore.stream().anyMatch(line ->
                            line.contains("+91 攻击伤害"));

                    loreMatches = hasLore1 && hasLore2 && hasLore4;
                }

                return nameMatches && loreMatches;
            }

            // 方法3：检查NBT特征（攻击伤害+91等）
            if (meta.hasAttributeModifiers()) {
                var modifiers = meta.getAttributeModifiers();
                if (modifiers != null) {
                    // 检查是否有高额的攻击伤害加成
                    return true; // 简化检查
                }
            }
        }

        return false;
    }

    private void showParticleRing(Location center) {
        if (activeEffects.contains(center)) {
            return;
        }

        activeEffects.add(center);

        new BukkitRunnable() {
            int duration = 0;
            final int maxDuration = 20 * 5; // 5秒

            @Override
            public void run() {
                if (duration++ >= maxDuration || !center.getWorld().isChunkLoaded(
                        center.getBlockX() >> 4, center.getBlockZ() >> 4)) {
                    activeEffects.remove(center);
                    cancel();
                    return;
                }

                // 创建紫色粒子环
                int particles = 20;
                double radius = 1.0;

                for (int i = 0; i < particles; i++) {
                    double angle = 2 * Math.PI * i / particles;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = center.clone().add(x, 0, z);

                    // 使用彩色粒子
                    center.getWorld().spawnParticle(
                            Particle.DUST_COLOR_TRANSITION,
                            particleLoc,
                            1,
                            0, 0, 0,
                            0,
                            new Particle.DustTransition(
                                    Color.fromRGB(200, 0, 255),  // 亮紫色
                                    Color.fromRGB(100, 0, 150),  // 暗紫色
                                    1.0f
                            )
                    );

                    // 添加一些闪烁的星星粒子
                    if (i % 5 == 0) {
                        center.getWorld().spawnParticle(
                                Particle.END_ROD,
                                particleLoc.clone().add(0, 0.5, 0),
                                1,
                                0.1, 0.1, 0.1,
                                0
                        );
                    }
                }

                // 随机播放音效
                if (duration % 10 == 0) {
                    center.getWorld().playSound(
                            center,
                            Sound.BLOCK_NOTE_BLOCK_CHIME,
                            0.5f,
                            1.5f
                    );
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * 标记物品为特殊物品
     */
    public void markAsSpecialItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(specialItemKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
    }

    /**
     * 清理所有粒子效果
     */
    public void cleanupAllEffects() {
        activeEffects.clear();
    }
}
package cn.mikudream.core.feature.shop;

import org.bukkit.Material;
import java.util.*;

public class MineralPricingSystem {
    private static final Map<Material, Integer> SELL_PRICES = new EnumMap<>(Material.class);
    private static final Map<Material, Integer> BUY_PRICES = new EnumMap<>(Material.class);
    public static double val = 0.00;

    static {
        // Initialize prices
        SELL_PRICES.put(Material.NETHERITE_INGOT, 288);
        SELL_PRICES.put(Material.DIAMOND, 125);
        SELL_PRICES.put(Material.EMERALD, 100);
        SELL_PRICES.put(Material.GOLD_INGOT, 40);
        SELL_PRICES.put(Material.IRON_INGOT, 25);
        SELL_PRICES.put(Material.COPPER_INGOT, 15);
        SELL_PRICES.put(Material.COAL, 5);
        SELL_PRICES.put(Material.LAPIS_LAZULI, 8);
        SELL_PRICES.put(Material.REDSTONE, 7);
        SELL_PRICES.put(Material.QUARTZ, 10);
        SELL_PRICES.put(Material.ANCIENT_DEBRIS, 128);
        SELL_PRICES.put(Material.DIAMOND_ORE, 100);
        SELL_PRICES.put(Material.EMERALD_ORE, 80);
        SELL_PRICES.put(Material.GOLD_ORE, 30);
        SELL_PRICES.put(Material.IRON_ORE, 20);
        SELL_PRICES.put(Material.COPPER_ORE, 12);
        SELL_PRICES.put(Material.COAL_ORE, 4);
        SELL_PRICES.put(Material.LAPIS_ORE, 6);
        SELL_PRICES.put(Material.REDSTONE_ORE, 5);
        SELL_PRICES.put(Material.NETHER_QUARTZ_ORE, 8);
        SELL_PRICES.put(Material.COAL_BLOCK,20);
        SELL_PRICES.put(Material.LAPIS_BLOCK,32);
        SELL_PRICES.put(Material.REDSTONE_BLOCK,28);
        SELL_PRICES.put(Material.QUARTZ_BLOCK,40);
        SELL_PRICES.put(Material.NETHERITE_BLOCK,648);
        SELL_PRICES.put(Material.DIAMOND_BLOCK, 478);
        SELL_PRICES.put(Material.EMERALD_BLOCK, 320);
        SELL_PRICES.put(Material.GOLD_BLOCK, 128);
        SELL_PRICES.put(Material.IRON_BLOCK, 82);
        SELL_PRICES.put(Material.COPPER_BLOCK, 48);

        // Set buy prices (7% higher than sell)
        for (Material material : SELL_PRICES.keySet()) {
            int sellPrice = SELL_PRICES.get(material);
            BUY_PRICES.put(material, (int) Math.round(sellPrice * 1.07));
        }
    }

    public static int getSellPrice(Material material) {
        return SELL_PRICES.getOrDefault(material, 0);
    }

    public static int getBuyPrice(Material material) {
        return BUY_PRICES.getOrDefault(material, 0);
    }

    public static boolean isSellable(Material material) {
        return SELL_PRICES.containsKey(material);
    }

    public static boolean isBuyable(Material material) {
        return BUY_PRICES.containsKey(material);
    }

    public static String getPriceList() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== 矿物价格表 ===\n");

        for (Material material : SELL_PRICES.keySet()) {
            int sellPrice = SELL_PRICES.get(material);
            int buyPrice = BUY_PRICES.get(material);

            sb.append(String.format("§e%s:\n", getDisplayName(material)));
            sb.append(String.format("  §a卖出价: §f%d coin\n", sellPrice));
            sb.append(String.format("  §f手续费: §f%d%%\n", (int)(val * 100)));
            sb.append(String.format("  §a实际价: §f%d coin\n", Math.round(sellPrice - sellPrice*val)));
            sb.append(String.format("  §f买入价: §f%d coin\n", buyPrice));
        }

        return sb.toString();
    }

    public static String getDisplayName(Material material) {
        return switch (material) {
            case NETHERITE_INGOT -> "下界合金锭";
            case DIAMOND -> "钻石";
            case DIAMOND_BLOCK -> "钻石块";
            case EMERALD -> "绿宝石";
            case EMERALD_BLOCK -> "绿宝石块";
            case GOLD_INGOT -> "金锭";
            case GOLD_BLOCK -> "金块";
            case IRON_INGOT -> "铁锭";
            case IRON_BLOCK -> "铁块";
            case COPPER_INGOT -> "铜锭";
            case COAL -> "煤炭";
            case LAPIS_LAZULI -> "青金石";
            case REDSTONE -> "红石";
            case QUARTZ -> "下界石英";
            case ANCIENT_DEBRIS -> "远古残骸";
            case DIAMOND_ORE -> "钻石矿石";
            case EMERALD_ORE -> "绿宝石矿石";
            case GOLD_ORE -> "金矿石";
            case IRON_ORE -> "铁矿石";
            case COPPER_ORE -> "铜矿石";
            case COAL_ORE -> "煤炭矿石";
            case LAPIS_ORE -> "青金石矿石";
            case REDSTONE_ORE -> "红石矿石";
            case NETHER_QUARTZ_ORE -> "下界石英矿石";
            case COAL_BLOCK -> "煤炭块";
            case LAPIS_BLOCK -> "青金石块";
            case REDSTONE_BLOCK -> "红石块";
            case QUARTZ_BLOCK -> "下界石英块";
            case NETHERITE_BLOCK -> "下界合金块";
            case COPPER_BLOCK -> "铜块";
            default -> material.name();
        };
    }

    public static List<Material> getBuyableMaterials() {
        return new ArrayList<>(BUY_PRICES.keySet());
    }
}

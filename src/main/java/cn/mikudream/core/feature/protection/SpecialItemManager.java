package cn.mikudream.core.feature.protection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * 特殊物品生成和验证器
 */
public class SpecialItemManager {
    private static final String SPECIAL_ITEM_ID = "special_endrod";

    /**
     * 创建一个特殊的末地烛物品（根据你提供的NBT）
     */
    public static ItemStack createSpecialEndRod() {
        ItemStack item = new ItemStack(Material.END_ROD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 设置显示名称
            meta.displayName(Component.text()
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("啊~你怎么能~到处~奖励呢~"))
                    .build());

            // 设置Lore
            List<Component> lore = Arrays.asList(
                    Component.text()
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text("想要吗来自爱卿的奖赏吗~"))
                            .build(),
                    Component.text()
                            .color(NamedTextColor.AQUA)
                            .append(Component.text("主人~拿在副手上~有很神奇的效果呢~"))
                            .build(),
                    Component.empty(),
                    Component.text()
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("+91 攻击伤害"))
                            .build()
            );
            meta.lore(lore);

            // 添加附魔（视觉效果）
            meta.addEnchant(Enchantment.UNBREAKING, 78, true);
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.addEnchant(Enchantment.BREACH, 3, true);
            meta.addEnchant(Enchantment.DENSITY, 3, true);
            meta.addEnchant(Enchantment.INFINITY, 1, true);

            // 添加属性修改器
            AttributeModifier attackDamage = new AttributeModifier(
                    NamespacedKey.minecraft("attack_damage"),
                    91.0,
                    AttributeModifier.Operation.ADD_NUMBER
            );

            AttributeModifier maxHealth = new AttributeModifier(
                    NamespacedKey.minecraft("max_health"),
                    10.0,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            meta.addAttributeModifier(Attribute.MAX_HEALTH, maxHealth);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, attackDamage);

            // 隐藏属性
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_UNBREAKABLE
            );

            // 设置不可破坏
            meta.setUnbreakable(true);

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 将物品转换为JSON NBT格式（用于/give命令）
     */
    public static String toGiveCommandNBT() {
        JsonObject itemNbt = new JsonObject();

        // 基础属性
        itemNbt.addProperty("id", "minecraft:end_rod");
        itemNbt.addProperty("Count", 1);

        // 物品标签
        JsonObject tag = new JsonObject();

        // CustomModelData (可选)
        tag.addProperty("CustomModelData", 9999);

        // 不可破坏
        tag.addProperty("Unbreakable", true);

        // 显示名称
        JsonObject display = new JsonObject();
        JsonObject name = new JsonObject();
        name.addProperty("text", "啊~你怎么能~到处~奖励呢~");
        name.addProperty("italic", false);
        name.addProperty("color", "light_purple");
        display.add("Name", name);

        // Lore
        JsonArray loreArray = new JsonArray();
        String[] loreTexts = {
                "{\"text\":\"想要吗来自爱卿的奖赏吗~\",\"color\":\"yellow\"}",
                "{\"text\":\"主人~拿在副手上~有很神奇的效果呢~\",\"color\":\"aqua\"}",
                "{\"text\":\"\"}",
                "{\"text\":\"+91 攻击伤害\",\"italic\":false,\"color\":\"green\"}"
        };
        for (String loreJson : loreTexts) {
            loreArray.add(JsonParser.parseString(loreJson));
        }
        display.add("Lore", loreArray);
        tag.add("display", display);

        // 附魔
        JsonObject enchantments = new JsonObject();
        enchantments.addProperty("efficiency", 78);
        enchantments.addProperty("sharpness", 5);
        enchantments.addProperty("breach", 3);
        enchantments.addProperty("infinity", 1);
        enchantments.addProperty("density", 3);
        tag.add("Enchantments", enchantments);

        // 属性修饰器
        JsonArray attributes = getJsonElements();

        tag.add("AttributeModifiers", attributes);

        // 可放置的方块
        JsonObject canPlaceOn = new JsonObject();
        JsonArray blocks = new JsonArray();
        blocks.add("minecraft:barrier");
        canPlaceOn.add("blocks", blocks);
        tag.add("CanPlaceOn", canPlaceOn);

        itemNbt.add("tag", tag);

        return itemNbt.toString();
    }

    private static @NonNull JsonArray getJsonElements() {
        JsonArray attributes = new JsonArray();

        // 攻击伤害
        JsonObject attackDamage = new JsonObject();
        attackDamage.addProperty("id", "base_attack_damage");
        attackDamage.addProperty("type", "minecraft:attack_damage");
        attackDamage.addProperty("amount", 91);
        attackDamage.addProperty("slot", "mainhand");
        attackDamage.addProperty("operation", "add_value");
        JsonObject attackDisplay = new JsonObject();
        attackDisplay.addProperty("type", "hidden");
        attackDamage.add("display", attackDisplay);
        attributes.add(attackDamage);

        // 最大生命值
        JsonObject maxHealth = new JsonObject();
        maxHealth.addProperty("id", "max_health");
        maxHealth.addProperty("type", "minecraft:max_health");
        maxHealth.addProperty("amount", 10);
        maxHealth.addProperty("slot", "offhand");
        maxHealth.addProperty("operation", "add_value");
        JsonObject healthDisplay = new JsonObject();
        healthDisplay.addProperty("type", "hidden");
        maxHealth.add("display", healthDisplay);
        attributes.add(maxHealth);

        // 挖掘速度（负值）
        JsonObject breakSpeed = new JsonObject();
        breakSpeed.addProperty("id", "block_break_speed");
        breakSpeed.addProperty("type", "minecraft:block_break_speed");
        breakSpeed.addProperty("amount", -999);
        breakSpeed.addProperty("slot", "mainhand");
        breakSpeed.addProperty("operation", "add_value");
        JsonObject speedDisplay = new JsonObject();
        speedDisplay.addProperty("type", "hidden");
        breakSpeed.add("display", speedDisplay);
        attributes.add(breakSpeed);
        return attributes;
    }

    /**
     * 获取完整的 /give 命令
     */
    public static String getGiveCommand(String playerName) {
        String nbt = toGiveCommandNBT();
        return String.format(
                "give %s minecraft:end_rod%s",
                playerName,
                nbt.replace("\"", "\\\"")
        );
    }
}
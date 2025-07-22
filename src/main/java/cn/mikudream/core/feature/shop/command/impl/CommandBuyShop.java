package cn.mikudream.core.feature.shop.command.impl;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.shop.MineralPricingSystem;
import cn.mikudream.core.feature.shop.command.ShopCommandInfo;
import cn.mikudream.core.feature.shop.command.ShopSubCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@ShopCommandInfo(
        name = "buy",
        purpose = "Buy items from the shop",
        syntax = "<item> [amount]"
)
public class CommandBuyShop extends ShopSubCommand {
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以使用此命令");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§c用法: /shop buy <物品> [数量]");
            return false;
        }

        Material material = Material.matchMaterial(args[1]);

        if (material == null || !MineralPricingSystem.isBuyable(material)) {
            sender.sendMessage("§c商品不存在或不可购买");
            return false;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) {
                    sender.sendMessage("§c数量必须大于0");
                    return false;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§c请输入有效的数量");
                return false;
            }
        }

        int pricePerItem = MineralPricingSystem.getBuyPrice(material);
        int totalPrice = pricePerItem * amount;

        CoinsManager coinsManager = new CoinsManager(MikuDream.getInstance());
        if (coinsManager.getCoins(player.getUniqueId()) < totalPrice) {
            sender.sendMessage(String.format("§c硬币不足，需要 %d 硬币 (当前有 %d)",
                    totalPrice,
                    coinsManager.getCoins(player.getUniqueId())));
            return false;
        }

        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            sender.sendMessage("§c背包已满，无法购买物品");
            return false;
        }

        // Deduct coins
        coinsManager.removeCoins(player.getUniqueId(), totalPrice);

        // Give items
        ItemStack itemToGive = new ItemStack(material, amount);
        player.getInventory().addItem(itemToGive);

        sender.sendMessage(String.format("§a成功购买 %d 个 %s，花费 %d 硬币",
                amount,
                MineralPricingSystem.getDisplayName(material),
                totalPrice));

        return true;
    }
}
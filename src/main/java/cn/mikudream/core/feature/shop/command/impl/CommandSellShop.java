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

import static cn.mikudream.core.feature.shop.MineralPricingSystem.val;

@ShopCommandInfo(
        name = "sell",
        purpose = "Sell items from your hand",
        syntax = "<amount>"
)
public class CommandSellShop extends ShopSubCommand {
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此命令");
            return true;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            sender.sendMessage("§c你手中没有物品");
            return false;
        }

        Material material = itemInHand.getType();
        if (!MineralPricingSystem.isSellable(material)) {
            sender.sendMessage("§c这个物品不能出售");
            return false;
        }

        int amount = itemInHand.getAmount();
        if (args.length > 1) {
            try {
                int requestedAmount = Integer.parseInt(args[1]);
                if (requestedAmount <= 0) {
                    sender.sendMessage("§c数量必须大于0");
                    return false;
                }
                amount = Math.min(requestedAmount, amount);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c请输入有效的数量");
                return false;
            }
        }

        int pricePerItem = MineralPricingSystem.getSellPrice(material);
        int totalPrice = pricePerItem * amount;

        // Remove items
        itemInHand.setAmount(itemInHand.getAmount() - amount);
        player.getInventory().setItemInMainHand(itemInHand.getAmount() > 0 ? itemInHand : null);

        // Add coins
        CoinsManager coinsManager = new CoinsManager(MikuDream.getInstance());
        coinsManager.addCoins(player.getUniqueId(), Math.round(totalPrice - totalPrice*val));

        sender.sendMessage(String.format("§a成功出售 %d 个 %s，获得 %d 硬币, 手续费 %d%%",
                amount,
                MineralPricingSystem.getDisplayName(material),
                Math.round(totalPrice - totalPrice*val),
                (int)(val * 100)));

        return true;
    }
}
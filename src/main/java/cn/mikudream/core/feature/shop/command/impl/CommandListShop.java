package cn.mikudream.core.feature.shop.command.impl;

import cn.mikudream.core.feature.shop.MineralPricingSystem;
import cn.mikudream.core.feature.shop.command.ShopCommandInfo;
import cn.mikudream.core.feature.shop.command.ShopSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@ShopCommandInfo(name="list", purpose="List all shop items")
public class CommandListShop extends ShopSubCommand {
    private static final String PERMISSION = "shop.list";
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }
            String price_list = MineralPricingSystem.getPriceList();
            sender.sendMessage(price_list);
        }
        return true;
    }
}

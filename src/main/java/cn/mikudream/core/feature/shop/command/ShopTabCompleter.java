package cn.mikudream.core.feature.shop.command;

import cn.mikudream.core.feature.shop.MineralPricingSystem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopTabCompleter implements TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("list", "buy", "sell");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if ("buy".equals(subCommand)) {
                return MineralPricingSystem.getBuyableMaterials().stream()
                        .map(Enum::name)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if ("sell".equals(subCommand)) {
                if (sender instanceof org.bukkit.entity.Player) {
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand.getType() != Material.AIR) {
                        int maxAmount = itemInHand.getAmount();
                        List<String> amounts = List.of("1", "8", "16", "32", "64", String.valueOf(maxAmount));
                        return amounts.stream()
                                .filter(amount -> amount.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                }
            }
        }

        return completions;
    }
}
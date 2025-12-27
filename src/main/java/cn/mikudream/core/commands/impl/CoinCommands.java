package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.AdminCommand;
import cn.mikudream.core.managers.CoinsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 硬币系统命令
 */
public class CoinCommands extends AdminCommand {
    private final CoinsManager coinsManager;

    public CoinCommands(CoinsManager coinsManager) {
        this.coinsManager = coinsManager;
    }

    @Override
    public String getName() {
        return "coin";
    }

    @Override
    public String getDescriptionText() {
        return "硬币系统命令";
    }

    @Override
    public String getUsage() {
        return "/coin <add|remove|list|reload|pay> [玩家] [数量]";
    }

    @Override
    public String getPermission() {
        return "coin.commands";
    }

    @Override
    public boolean executeAdmin(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return true;
        }

        if (args.length == 0) {
            return showHelp(sender);
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender, args);
            // case "reload" -> handleReload(sender, args);
            case "pay" -> handlePay(sender, args);
            default -> showHelp(sender);
        };
    }

    @Override
    public Optional<List<String>> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!validateSender(sender)) {
            return Optional.empty();
        }

        if (args.length == 1) {
            List<String> subCommands = List.of("add", "remove", "list", "reload", "pay");
            return Optional.of(subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            List<String> suggestions = switch (subCommand) {
                case "add", "remove", "list" -> Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
                case "pay" -> getOnlinePlayersExcept(sender.getName())
                        .map(Player::getName)
                        .collect(Collectors.toList());
                default -> List.of();
            };

            return Optional.of(suggestions.stream()
                    .filter(name -> name.startsWith(args[1]))
                    .collect(Collectors.toList()));
        }

        return Optional.empty();
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /coin add <玩家> <数量>");
            return true;
        }

        try {
            String targetName = args[1];
            long amount = Long.parseLong(args[2]);

            if (amount <= 0) {
                sender.sendMessage("§c数量必须大于0!");
                return true;
            }

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§c玩家不存在或不在线!");
                return true;
            }

            coinsManager.addCoins(target.getUniqueId(), amount);
            sender.sendMessage("§a已为 " + target.getName() + " 增加 " + amount + " 硬币");
            target.sendMessage("§e你获得了 " + amount + " 硬币");

        } catch (NumberFormatException e) {
            sender.sendMessage("§c数量必须是有效的数字!");
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /coin remove <玩家> <数量>");
            return true;
        }

        try {
            String targetName = args[1];
            long amount = Long.parseLong(args[2]);

            if (amount <= 0) {
                sender.sendMessage("§c数量必须大于0!");
                return true;
            }

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§c玩家不存在或不在线!");
                return true;
            }

            long current = coinsManager.getCoins(target.getUniqueId());
            if (current < amount) {
                sender.sendMessage("§c玩家硬币不足!");
                return true;
            }

            coinsManager.removeCoins(target.getUniqueId(), amount);
            sender.sendMessage("§a已为 " + target.getName() + " 减少 " + amount + " 硬币");
            target.sendMessage("§e你失去了 " + amount + " 硬币");

        } catch (NumberFormatException e) {
            sender.sendMessage("§c数量必须是有效的数字!");
        }

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (args.length >= 2 && sender.hasPermission("coin.admin")) {
            // 查询其他玩家
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                sender.sendMessage("§c玩家不存在或不在线!");
                return true;
            }

            long coins = coinsManager.getCoins(target.getUniqueId());
            sender.sendMessage("§e玩家 " + target.getName() + " 当前有 " + coins + " 硬币");

        } else if (sender instanceof Player player) {
            // 查询自己
            long coins = coinsManager.getCoins(player.getUniqueId());
            sender.sendMessage("§e你的硬币余额: " + coins);

        } else {
            sender.sendMessage("§c控制台只能查询其他玩家!");
        }

        return true;
    }

//    private boolean handleReload(CommandSender sender, String[] args) {
//        coinsManager.reload();
//        sender.sendMessage("§a硬币数据已重载!");
//        return true;
//    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以转账!");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§c用法: /coin pay <玩家> <数量>");
            return true;
        }

        try {
            String targetName = args[1];
            long amount = Long.parseLong(args[2]);

            if (amount <= 0) {
                player.sendMessage("§c数量必须大于0!");
                return true;
            }

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                player.sendMessage("§c玩家不存在或不在线!");
                return true;
            }

            if (player.equals(target)) {
                player.sendMessage("§c不能转账给自己!");
                return true;
            }

            long playerCoins = coinsManager.getCoins(player.getUniqueId());
            if (playerCoins < amount) {
                player.sendMessage("§c你的硬币不足!");
                return true;
            }

            // 执行转账
            coinsManager.removeCoins(player.getUniqueId(), amount);
            coinsManager.addCoins(target.getUniqueId(), amount);

            player.sendMessage("§a你向 " + target.getName() + " 转账了 " + amount + " 硬币");
            target.sendMessage("§e你收到了 " + player.getName() + " 转账的 " + amount + " 硬币");

        } catch (NumberFormatException e) {
            sender.sendMessage("§c数量必须是有效的数字!");
        }

        return true;
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("§6§l硬币系统命令");
        sender.sendMessage("§e/coin add <玩家> <数量> §7- 增加硬币");
        sender.sendMessage("§e/coin remove <玩家> <数量> §7- 减少硬币");
        sender.sendMessage("§e/coin list [玩家] §7- 查看余额");
        sender.sendMessage("§e/coin reload §7- 重载数据");
        sender.sendMessage("§e/coin pay <玩家> <数量> §7- 转账给玩家");
        return true;
    }

    private Stream<? extends Player> getOnlinePlayersExcept(String exceptName) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getName().equalsIgnoreCase(exceptName));
    }
}
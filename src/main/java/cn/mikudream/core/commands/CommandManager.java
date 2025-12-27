package cn.mikudream.core.commands;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.commands.impl.*;
import cn.mikudream.core.feature.friend.FriendSystem;
import cn.mikudream.core.feature.lottery.LotterySystem;
import cn.mikudream.core.managers.*;
import org.bukkit.command.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一命令管理器
 */
public class CommandManager {
    private final MikuDream plugin;
    private final Map<String, BaseCommand> commands = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> dependencies = new ConcurrentHashMap<>();
    private final Set<UUID> invisiblePlayers;

    public CommandManager(MikuDream plugin, Set<UUID> invisiblePlayers) {
        this.plugin = plugin;
        this.invisiblePlayers = invisiblePlayers;
        registerDependencies();
        registerCommands();
    }

    private void registerDependencies() {
        ManagerFactory managerFactory = plugin.getManagerFactory();

        dependencies.put(CoinsManager.class, managerFactory.getCoinsManager());
        dependencies.put(FriendSystem.class, managerFactory.getFriendSystem());
        dependencies.put(ShopManager.class, managerFactory.getShopManager());
        dependencies.put(PlayerMarketManager.class, managerFactory.getMarketManager());
        dependencies.put(LotterySystem.class, managerFactory.getLotterySystem());
    }

    @SuppressWarnings("unchecked")
    private <T> T getDependency(Class<T> clazz) {
        return (T) dependencies.get(clazz);
    }

    private void registerCommands() {
        // 好友系统命令
        registerCommand("friend", new FriendCommands(
                getDependency(FriendSystem.class)
        ));

        // 硬币系统命令
        registerCommand("coin", new CoinCommands(
                getDependency(CoinsManager.class)
        ));

        // 商店命令
        registerCommand("shop", new ShopCommand());

        // 玩家市场命令
        registerCommand("market", new MarketCommand());

        // 抽奖系统命令
        registerCommand("lottery", new LotteryCommand(
                getDependency(LotterySystem.class)
        ));

        // 其他系统命令
        registerCommand("hub", new HubCommand(plugin));
        registerCommand("skill", new SkillCommand());
        registerCommand("sv", new InvisibleCommand(plugin, invisiblePlayers));
        registerCommand("catconfig", new ConfigCommands(plugin));
    }

    private void registerCommand(String name, BaseCommand command) {
        commands.put(name, command);

        // 注册到Bukkit
        PluginCommand pluginCommand = plugin.getCommand(name);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(new DelegatingCommandExecutor(command));
            pluginCommand.setTabCompleter(new DelegatingTabCompleter(command));
        } else {
            plugin.getLogger().warning("无法注册命令: " + name + " - 检查plugin.yml");
        }
    }

    /**
         * 委托命令执行器
         */
        private record DelegatingCommandExecutor(BaseCommand command) implements CommandExecutor {

        @Override
            public boolean onCommand(CommandSender sender, Command cmd,
                                     String label, String[] args) {
                return command.execute(sender, cmd, label, args);
            }
        }

    /**
         * 委托Tab补全器
         */
        private record DelegatingTabCompleter(BaseCommand command) implements TabCompleter {

        @Override
            public List<String> onTabComplete(CommandSender sender, Command cmd,
                                              String label, String[] args) {
                return command.tabComplete(sender, cmd, label, args)
                        .orElse(List.of());
            }
        }
}
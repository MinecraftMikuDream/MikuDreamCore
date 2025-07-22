package cn.mikudream.core.command;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.command.impl.*;
import cn.mikudream.core.feature.breakboard.BreakBoardManager;
import cn.mikudream.core.feature.breakboard.BreakListener;
import cn.mikudream.core.feature.coin.CoinsManager;
import org.bukkit.Server;

public class SCommand {
    private final MikuDream plugin = MikuDream.getInstance();
    public void init()
    {
        BreakBoardManager breakBoardManager = new BreakBoardManager();
        CoinsManager mikuCoinsManager = new CoinsManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(new BreakListener(breakBoardManager), plugin);
        try {
            plugin.getCommand("sbreakboard").setExecutor(new BreakBoardCommand(breakBoardManager));
            plugin.getCommand("skill").setExecutor(new SKill());
            plugin.getCommand("sconfig").setExecutor(new SConfigCommand(plugin));
            plugin.getCommand("hub").setExecutor(new Hub(plugin));
            plugin.getCommand("sv").setExecutor(new SVCommand());
            plugin.getCommand("srf").setExecutor(new SRFCommand(plugin, mikuCoinsManager));
        } catch (Exception e) {
            Server server = plugin.getServer();
            server.getLogger().severe("注册命令时出现错误: " + e.getMessage());
        }
    }
}

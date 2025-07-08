package cn.mikudream.core.command;

import cn.mikudream.core.MikuDream;
import cn.mikudream.core.command.impl.*;
import cn.mikudream.core.feature.breakboard.BreakBoardManager;
import cn.mikudream.core.feature.breakboard.BreakListener;
import cn.mikudream.core.feature.scoin.SCoinManager;
import org.bukkit.Bukkit;

public class SCommand {
    private final MikuDream plugin = MikuDream.getInstance();
    public void init()
    {
        BreakBoardManager breakBoardManager = new BreakBoardManager();
        SCoinManager sCoinManager = new SCoinManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(new BreakListener(breakBoardManager), plugin);
        try {
            plugin.getCommand("sbreakboard").setExecutor(new BreakBoardCommand(breakBoardManager));
            plugin.getCommand("skill").setExecutor(new SKill());
            plugin.getCommand("sconfig").setExecutor(new SConfigCommand(plugin));
            plugin.getCommand("hub").setExecutor(new Hub(plugin));
        } catch (Exception e) {
            Bukkit bukkit;
            Bukkit.getLogger().severe("注册命令时出现错误: " + e.getMessage());
        }
    }
}

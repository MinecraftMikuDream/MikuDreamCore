package cn.mikudream.core.feature.coin.command.impl;

import cn.mikudream.core.feature.coin.CoinsManager;
import cn.mikudream.core.feature.coin.command.CoinCommandInfo;
import cn.mikudream.core.feature.coin.command.CoinSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@CoinCommandInfo(name="reload", purpose="Reload Coin")
public class CommandReload extends CoinSubCommand {
    public CoinsManager coinManager;

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("coin.reload")) {
            sender.sendMessage("你没有权限");
            return true;
        }
        coinManager.reload();
        return true;
    }
}

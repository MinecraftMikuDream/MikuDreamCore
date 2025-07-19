package cn.mikudream.core.feature.scoin.command.impl;

import cn.mikudream.core.feature.scoin.SCoinManager;
import cn.mikudream.core.feature.scoin.command.CommandInfo;
import cn.mikudream.core.feature.scoin.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@CommandInfo(name="reload", purpose="Reload SCoin")
public class CommandReload extends SubCommand {
    public SCoinManager coinManager;

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("scoin.reload")) {
            sender.sendMessage("你没有权限");
            return true;
        }
        coinManager.reload();
        sender.sendMessage("金币数据重新加载完成");
        return true;
    }
}

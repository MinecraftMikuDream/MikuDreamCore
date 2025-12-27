package cn.mikudream.core.commands.impl;

import cn.mikudream.core.commands.PlayerCommand;
import cn.mikudream.core.feature.lottery.LotterySystem;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * 抽奖命令
 */
public class LotteryCommand extends PlayerCommand {
    private final LotterySystem lotterySystem;

    public LotteryCommand(LotterySystem lotterySystem) {
        this.lotterySystem = lotterySystem;
    }

    @Override
    public String getName() {
        return "lottery";
    }

    @Override
    public String getDescriptionText() {
        return "抽奖系统";
    }

    @Override
    public String getUsage() {
        return "/lottery";
    }

    @Override
    protected boolean executePlayer(Player player, Command command, String label, String[] args) {
        return lotterySystem.startNormalLottery(player);
    }
}